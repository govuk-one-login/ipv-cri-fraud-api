package uk.gov.di.ipv.cri.fraud.dynamotest.service;

import com.nimbusds.oauth2.sdk.token.AccessToken;
import uk.gov.di.ipv.cri.common.library.exception.AccessTokenExpiredException;
import uk.gov.di.ipv.cri.common.library.exception.AuthorizationCodeExpiredException;
import uk.gov.di.ipv.cri.common.library.exception.SessionExpiredException;
import uk.gov.di.ipv.cri.common.library.exception.SessionNotFoundException;
import uk.gov.di.ipv.cri.common.library.persistence.DataStore;
import uk.gov.di.ipv.cri.common.library.service.ConfigurationService;
import uk.gov.di.ipv.cri.common.library.util.ListUtil;
import uk.gov.di.ipv.cri.fraud.dynamotest.domain.DBTestSessionItem;

import java.time.Clock;
import java.util.UUID;

public class DBTestSessionService {
    private final ConfigurationService configurationService;
    private final DataStore<DBTestSessionItem> dataStore;
    private final ListUtil listUtil;
    private final Clock clock;

    public DBTestSessionService(
            DataStore<DBTestSessionItem> dataStore,
            ConfigurationService configurationService,
            Clock clock,
            ListUtil listUtil) {
        this.dataStore = dataStore;
        this.configurationService = configurationService;
        this.clock = clock;
        this.listUtil = listUtil;
    }

    public void updateSession(DBTestSessionItem sessionItem) {
        dataStore.update(sessionItem);
    }

    public void createAuthorizationCode(DBTestSessionItem session) {
        session.setAuthorizationCode(UUID.randomUUID().toString());
        session.setAuthorizationCodeExpiryDate(
                configurationService.getAuthorizationCodeExpirationEpoch());
        updateSession(session);
    }

    public DBTestSessionItem validateSessionId(String sessionId)
            throws SessionNotFoundException, SessionExpiredException {

        DBTestSessionItem sessionItem = dataStore.getItem(sessionId);
        if (sessionItem == null) {
            throw new SessionNotFoundException("session not found");
        }

        if (sessionItem.getExpiryDate() < clock.instant().getEpochSecond()) {
            throw new SessionExpiredException("session expired");
        }

        return sessionItem;
    }

    public DBTestSessionItem getSession(String sessionId) {
        return dataStore.getItem(sessionId);
    }

    public DBTestSessionItem getSessionByAccessToken(AccessToken accessToken)
            throws SessionExpiredException, AccessTokenExpiredException, SessionNotFoundException {
        DBTestSessionItem sessionItem;

        try {
            sessionItem =
                    listUtil.getOneItemOrThrowError(
                            dataStore.getItemByIndex(
                                    DBTestSessionItem.ACCESS_TOKEN_INDEX,
                                    accessToken.toAuthorizationHeader()));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("No items found")) {
                throw new SessionNotFoundException("no session found with that access token");
            } else {
                throw new SessionNotFoundException(
                        "more than one session found with that access token");
            }
        }

        // Re-fetch our session directly to avoid problems with projections
        sessionItem = validateSessionId(String.valueOf(sessionItem.getSessionId()));
        if (sessionItem.getAccessTokenExpiryDate() < clock.instant().getEpochSecond()) {
            throw new AccessTokenExpiredException("access code expired");
        }

        return sessionItem;
    }

    public DBTestSessionItem getSessionByAuthorisationCode(String authCode)
            throws SessionExpiredException, AuthorizationCodeExpiredException,
                    SessionNotFoundException {
        DBTestSessionItem sessionItem;

        try {
            sessionItem =
                    listUtil.getOneItemOrThrowError(
                            dataStore.getItemByIndex(
                                    DBTestSessionItem.AUTHORIZATION_CODE_INDEX, authCode));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("No items found")) {
                throw new SessionNotFoundException("no session found with that authorization code");
            } else {
                throw new SessionNotFoundException(
                        "more than one session found with that authorization code");
            }
        }

        // Re-fetch our session directly to avoid problems with projections
        sessionItem = validateSessionId(String.valueOf(sessionItem.getSessionId()));

        if (sessionItem.getAuthorizationCodeExpiryDate() < clock.instant().getEpochSecond()) {
            throw new AuthorizationCodeExpiredException("authorization code expired");
        }

        return sessionItem;
    }
}
