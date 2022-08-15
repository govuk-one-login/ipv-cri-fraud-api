package uk.gov.di.ipv.cri.fraud.api.gateway;

import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PEPRequestMapper extends IdentityVerificationRequestMapper {
    public PEPRequestMapper(String tenantId) {
        super(tenantId);
    }

    public PEPRequest mapPersonIdentity(PersonIdentity personIdentity) {
        Objects.requireNonNull(personIdentity, "The personIdentity must not be null");

        Header apiRequestHeader = createPEPApiRequestHeader();

        Payload apiRequestPayload = new Payload();
        Contact contact = new Contact();
        contact.setId("MAINCONTACT_1");

        PersonDetails contactPersonDetails = new PersonDetails();
        contactPersonDetails.setDateOfBirth(
                DateTimeFormatter.ISO_DATE.format(personIdentity.getDateOfBirth()));
        contactPersonDetails.setPepsSanctionsFlag("Y");
        contactPersonDetails.setYearOfBirth(
                String.valueOf(personIdentity.getDateOfBirth().getYear()));

        Name contactPersonName = mapName(personIdentity);
        List<Address> personAddresses = mapAddresses(personIdentity.getAddresses());

        Person contactPerson = new Person();
        contactPerson.setPersonIdentifier(null);
        contactPerson.setPersonDetails(contactPersonDetails);
        contactPerson.setNames(List.of(contactPersonName));
        contactPerson.setTypeOfPerson("APPLICANT");
        contact.setPerson(contactPerson);
        contact.setAddresses(personAddresses);

        Applicant applicant = createPEPApplicant();
        Application application = new Application();
        application.setType("OTHER");
        application.setApplicants(List.of(applicant));
        ProductDetails productDetails = new ProductDetails();
        productDetails.setProductCode("MS_CON");
        application.setProductDetails(productDetails);
        application.setStatus("PENDING");
        application.setOriginalRequestTime(
                Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());

        apiRequestPayload.setApplication(application);
        apiRequestPayload.setSource(null);
        apiRequestPayload.setContacts(List.of(contact));

        PEPRequest apiRequest = new PEPRequest();
        apiRequest.setHeader(apiRequestHeader);
        apiRequest.setPayload(apiRequestPayload);

        return apiRequest;
    }

    private Header createPEPApiRequestHeader() {
        Header apiRequestHeader = new Header();
        apiRequestHeader.setTenantId(super.tenantId);
        apiRequestHeader.setRequestType("PepSanctions01");
        apiRequestHeader.setClientReferenceId(UUID.randomUUID().toString());
        apiRequestHeader.setMessageTime(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        apiRequestHeader.setOptions(new Options());
        return apiRequestHeader;
    }

    Applicant createPEPApplicant() {
        Applicant applicant = new Applicant();
        applicant.setId("APPLICANT_1");
        applicant.setContactId("MAINCONTACT_1");
        applicant.setType(null);
        applicant.setApplicantType("APPLICANT");
        applicant.setConsent(true);
        return applicant;
    }
}
