package uk.gov.di.ipv.cri.fraud.api.gateway;

import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.Address;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.Applicant;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.Application;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.Contact;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.Header;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.IdentityVerificationRequest;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.Name;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.Options;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.Payload;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.Person;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.PersonDetails;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class IdentityVerificationRequestMapper {

    private final String tenantId;

    public IdentityVerificationRequestMapper(String tenantId) {
        this.tenantId = tenantId;
    }

    public IdentityVerificationRequest mapPersonIdentity(PersonIdentity personIdentity) {
        Objects.requireNonNull(personIdentity, "The personIdentity must not be null");

        Header apiRequestHeader = createApiRequestHeader();

        Payload apiRequestPayload = new Payload();
        Contact contact = new Contact();
        contact.setId("MAINCONTACT_1");

        PersonDetails contactPersonDetails = new PersonDetails();
        contactPersonDetails.setDateOfBirth(
                DateTimeFormatter.ISO_DATE.format(personIdentity.getDateOfBirth()));

        Name contactPersonName = mapName(personIdentity);
        List<Address> personAddresses = mapAddresses(personIdentity.getAddresses());

        Person contactPerson = new Person();
        contactPerson.setPersonIdentifier("MAINPERSON_1");
        contactPerson.setPersonDetails(contactPersonDetails);
        contactPerson.setNames(List.of(contactPersonName));

        contact.setPerson(contactPerson);
        contact.setAddresses(personAddresses);

        Applicant applicant = createApplicant();
        Application application = new Application();
        application.setApplicants(List.of(applicant));

        apiRequestPayload.setApplication(application);
        apiRequestPayload.setSource("WEB");
        apiRequestPayload.setContacts(List.of(contact));

        IdentityVerificationRequest apiRequest = new IdentityVerificationRequest();
        apiRequest.setHeader(apiRequestHeader);
        apiRequest.setPayload(apiRequestPayload);

        return apiRequest;
    }

    private Header createApiRequestHeader() {
        Header apiRequestHeader = new Header();
        apiRequestHeader.setTenantId(this.tenantId);
        apiRequestHeader.setRequestType("Authenticateplus-Standalone");
        apiRequestHeader.setClientReferenceId(UUID.randomUUID().toString());
        apiRequestHeader.setMessageTime(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        apiRequestHeader.setOptions(new Options());
        return apiRequestHeader;
    }

    private Name mapName(PersonIdentity personIdentity) {
        Name personName = new Name();
        personName.setId("MAINPERSONNAME_1");
        personName.setType("CURRENT");
        personName.setFirstName(personIdentity.getFirstName());
        personName.setMiddleNames(personIdentity.getMiddleNames());
        personName.setSurName(personIdentity.getSurname());
        return personName;
    }

    private Applicant createApplicant() {
        Applicant applicant = new Applicant();
        applicant.setId("APPLICANT_1");
        applicant.setContactId("MAINCONTACT_1");
        applicant.setType("INDIVIDUAL");
        applicant.setApplicantType("MAIN_APPLICANT");
        applicant.setConsent(true);
        return applicant;
    }

    private List<Address> mapAddresses(
            List<uk.gov.di.ipv.cri.common.library.domain.personidentity.Address> personAddresses) {
        List<Address> addresses = new ArrayList<>();
        AtomicInteger addressId = new AtomicInteger(0);
        personAddresses.forEach(
                personAddress -> {
                    Address address = new Address();

                    address.setId("MAINAPPADDRESS_" + addressId.incrementAndGet());
                    address.setAddressIdentifier("ADDRESS_" + addressId.get());

                    String addressType = mapAddressType(personAddress.getAddressType());
                    address.setAddressType(addressType);

                    address.setBuildingNumber(personAddress.getBuildingNumber());
                    address.setBuildingName(personAddress.getBuildingName());
                    address.setSubBuilding(personAddress.getSubBuildingName());
                    address.setStreet(personAddress.getStreetName());
                    address.setPostTown(personAddress.getAddressLocality());

                    address.setPostal(personAddress.getPostalCode());

                    addresses.add(address);
                });

        return addresses;
    }

    private String mapAddressType(
            uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType addressType) {
        switch (addressType) {
            case CURRENT:
                return "CURRENT";
            case PREVIOUS:
                return "PREVIOUS";
            default:
                throw new IllegalArgumentException(
                        "Unexpected addressType encountered: " + addressType);
        }
    }
}
