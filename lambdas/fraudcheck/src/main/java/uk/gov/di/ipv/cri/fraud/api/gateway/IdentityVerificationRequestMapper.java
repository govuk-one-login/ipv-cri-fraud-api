package uk.gov.di.ipv.cri.fraud.api.gateway;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.AddressType;
import uk.gov.di.ipv.cri.common.library.domain.personidentity.PersonIdentity;
import uk.gov.di.ipv.cri.fraud.api.gateway.dto.request.*;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class IdentityVerificationRequestMapper {

    private static final Logger LOGGER = LogManager.getLogger();
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
        List<Address> personAddresses = mapMultipleAddresses(personIdentity.getAddresses());

        Person contactPerson = new Person();
        contactPerson.setPersonIdentifier("MAINPERSON_1");
        contactPerson.setPersonDetails(contactPersonDetails);
        contactPerson.setNames(List.of(contactPersonName));

        contact.setPerson(contactPerson);
        contact.setAddresses(personAddresses);

        Applicant applicant = createApplicant();
        Application application = new Application();
        application.setApplicants(List.of(applicant));
        application.setProductDetails(null);
        // application.setOriginalRequestTime(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());

        apiRequestPayload.setApplication(application);
        apiRequestPayload.setSource("WEB");
        apiRequestPayload.setContacts(List.of(contact));

        IdentityVerificationRequest apiRequest = new IdentityVerificationRequest();
        apiRequest.setHeader(apiRequestHeader);
        apiRequest.setPayload(apiRequestPayload);

        return apiRequest;
    }

    public PEPRequest mapPEPPersonIdentity(PersonIdentity personIdentity) {
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
        List<Address> personAddresses = mapSingleAddress(personIdentity.getAddresses());

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
        apiRequestHeader.setTenantId(this.tenantId);
        apiRequestHeader.setRequestType("PepSanctions01");
        apiRequestHeader.setClientReferenceId(UUID.randomUUID().toString());
        apiRequestHeader.setMessageTime(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        apiRequestHeader.setOptions(new Options());
        return apiRequestHeader;
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

    private Applicant createPEPApplicant() {
        Applicant applicant = new Applicant();
        applicant.setId("APPLICANT_1");
        applicant.setContactId("MAINCONTACT_1");
        applicant.setType(null);
        applicant.setApplicantType("APPLICANT");
        applicant.setConsent(true);
        return applicant;
    }

    private List<Address> mapMultipleAddresses(
            List<uk.gov.di.ipv.cri.common.library.domain.personidentity.Address> personAddresses) {
        List<Address> addresses = new ArrayList<>();
        AtomicInteger addressId = new AtomicInteger(0);
        personAddresses.forEach(
                personAddress -> {
                    Address address = mapAddressFromPersonAddress(addressId, personAddress);
                    addresses.add(address);
                });

        return addresses;
    }

    private List<Address> mapSingleAddress(
            List<uk.gov.di.ipv.cri.common.library.domain.personidentity.Address> personAddresses) {
        List<Address> addresses = new ArrayList<>();
        AtomicInteger addressId = new AtomicInteger(0);
        personAddresses.forEach(
                personAddress -> {
                    if (AddressType.CURRENT == personAddress.getAddressType()) {
                        Address address = mapAddressFromPersonAddress(addressId, personAddress);
                        addresses.add(address);
                    }
                });

        // PEP can only be sent with one address
        if (addresses.size() > 1) {
            LOGGER.error("mapSingleAddress Found {} CURRENT Addresses", addresses.size());
        }

        return addresses;
    }

    private Address mapAddressFromPersonAddress(
            AtomicInteger addressId,
            uk.gov.di.ipv.cri.common.library.domain.personidentity.Address personAddress) {

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

        return address;
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
