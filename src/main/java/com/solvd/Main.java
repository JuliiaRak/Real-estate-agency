package com.solvd;

import com.solvd.domain.*;
import com.solvd.domain.enums.RealEstateType;
import com.solvd.domain.exceptions.EmailAlreadyExistsException;
import com.solvd.domain.exceptions.EntityNotFoundException;
import com.solvd.domain.exceptions.LinkAlreadyExistsException;
import com.solvd.domain.exceptions.PhoneNumberAlreadyExistsException;
import com.solvd.service.*;
import com.solvd.service.impl.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Main {
    private static final ClientService CLIENT_SERVICE = new ClientServiceImpl();
    private static final AddressService ADDRESS_SERVICE = new AddressServiceImpl();
    private static final RealEstateService REAL_ESTATE_SERVICE = new RealEstateServiceImpl();
    private static final AgreementService AGREEMENT_SERVICE = new AgreementServiceImpl();
    private static final MeetingService MEETING_SERVICE = new MeetingServiceImpl();
    private static final EmployeeService EMPLOYEE_SERVICE = new EmployeeServiceImpl();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean exitLoop = false;
        System.out.println("Welcome to the Real Estate Agency console app. Please choose an action");

        while (!exitLoop) {
            System.out.println("\nEnter '0' to register as a new client");
            System.out.println("Enter '1' to login and confirm your identity.");
            System.out.println("Enter '2' to Exit.");

            String input = scanner.nextLine();

            switch (input) {
                case "0":
                    registerClient(scanner);
                    break;
                case "1":
                    login(scanner);
                    break;
                case "2":
                    exitLoop = true;
                    break;
                default:
                    System.out.println("Invalid option. Please enter '0', '1' or '2'.");
            }
        }
    }

    private static void registerClient(Scanner scanner) {
        System.out.println("Please enter your registration details:");
        System.out.print("1. Enter your first name: ");
        String firstName = scanner.nextLine();
        System.out.print("2. Enter your last name: ");
        String lastName = scanner.nextLine();
        System.out.print("3. Enter your email: ");
        String email = scanner.nextLine();
        System.out.print("4. Enter your phone number: ");
        String phoneNumber = scanner.nextLine();

        Client client = Client.builder()
                .setFirstName(firstName)
                .setLastName(lastName)
                .setEmail(email)
                .setPhoneNumber(phoneNumber)
                .setRegistrationDate(new Date())
                .build();

        try {
            CLIENT_SERVICE.create(client);
        } catch (IllegalArgumentException | NullPointerException |
                 PhoneNumberAlreadyExistsException | EmailAlreadyExistsException e) {
            System.out.println("\n" + e.getMessage());
            System.out.println("Please try again.");
            return;
        }

        System.out.println("\n" + "Thank you for registration!");

        try {
            userActions(scanner, client);
        } catch (IllegalArgumentException | NullPointerException |
                 EntityNotFoundException | LinkAlreadyExistsException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void login(Scanner scanner) {
        Client client = new Client();

        System.out.println("Please enter your logIn details:");
        System.out.print("Please enter your email: ");
        String email = scanner.nextLine();
        System.out.print("Please enter your phone number: ");
        String phoneNumber = scanner.nextLine();

        try {
            Client clientByEmail = CLIENT_SERVICE.getByEmail(email);
            Client clientByPhone = CLIENT_SERVICE.getByPhoneNumber(phoneNumber);
            if (clientByEmail.getId() == clientByPhone.getId()) {
                client = clientByEmail;
            } else {
                System.out.println("Email and phone don`t match");
                return;
            }

        } catch (IllegalArgumentException | NullPointerException | EntityNotFoundException e) {
            System.out.println("\n" + e.getMessage());
            System.out.println("Please try again.");
            return;
        }

        System.out.println("\n" + "You've successfully Logged In. Hello " + client.getFirstName() + " ! ");

        try {
            userActions(scanner, client);
        } catch (IllegalArgumentException | NullPointerException |
                 EntityNotFoundException | LinkAlreadyExistsException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void userActions(Scanner scanner, Client client) throws EntityNotFoundException, LinkAlreadyExistsException {
        boolean exitLoop = false;
        while (!exitLoop) {
            System.out.println("\nNow choose an action (write a number):");
            System.out.println("1. Put new real estate up for sale.");
            System.out.println("2. View real estate by type.");
            System.out.println("3. View all real estates.");
            System.out.println("4. View my real estates");
            System.out.println("5. Order real estate");
            System.out.println("6. Delete account.");
            System.out.println("7. View my ordered.");
            System.out.println("8. View my meetings.");
            System.out.println("9. Settings.");
            System.out.println("10. Exit.");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    createRealEstate(scanner, client);
                    break;
                case "2":
                    viewRealEstateByType(scanner);
                    break;
                case "3":
                    viewAllRealEstates();
                    break;
                case "4":
                    List<RealEstate> allRealEstates = REAL_ESTATE_SERVICE.getAllBySeller(client);
                    if (allRealEstates.isEmpty()) {
                        System.out.println("You do not have real estates");
                        break;
                    }
                    for (RealEstate rlSt : allRealEstates) {
                        System.out.println(rlSt);
                    }
                    break;
                case "5":
                    System.out.println("You want to create a meet to view Real Estate or you ready to buy?\n" +
                            "1. Create a meeting\n" +
                            "2. Create a order");
                    String choose = scanner.nextLine();
                    try {
                        switch (choose) {
                            case "1":
                                Employee employee = chooseEmployee(scanner);
                                createMeeting(scanner, client, employee);
                                break;
                            case "2":
                                orderRealEstate(scanner, client);
                                break;
                        }
                    } catch (IllegalArgumentException | NullPointerException | EntityNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case "6":
                    deleteAccount(scanner, client);
                    break;
                case "7":
                    System.out.println(AGREEMENT_SERVICE.getByClientId(client.getId()));
                    break;
                case "8":
                    try {
                        viewClientsMeetings(scanner, client);
                    } catch (EntityNotFoundException e) {
                        System.out.println(e.getMessage());
                    }
                    break;
                case "9":
                    settings(scanner, client);
                    break;
                case "10":
                    exitLoop = true;
                    break;
                default:
                    System.out.println("Invalid option.");
            }

            // Add more cases for other actions as needed
        }
    }

    public static void createRealEstate(Scanner scanner, Client client) {
        Address address = new Address();
        RealEstate realEstate = new RealEstate();

        System.out.println("Please enter real estate address details:");
        System.out.print("1. Enter country: ");
        String country = scanner.nextLine();
        System.out.print("2. Enter region: ");
        String region = scanner.nextLine();
        System.out.print("3. Enter city: ");
        String city = scanner.nextLine();
        System.out.print("4. Enter street: ");
        String street = scanner.nextLine();
        System.out.print("5. Enter building: ");
        String building = scanner.nextLine();
        System.out.print("6. Enter apartment: ");
        String apartment = scanner.nextLine();

        address.setCountry(country);
        address.setRegion(region);
        address.setCity(city);
        address.setStreet(street);
        address.setBuilding(building);
        address.setApartment(apartment);

        System.out.println("Please enter real estate details:");
        System.out.print("1. Enter price: ");
        String price = scanner.nextLine();
        System.out.print("2. Enter description: ");
        String description = scanner.nextLine();

        boolean badType = true;
        do {
            System.out.print("3. Enter Real Estate Type: ");
            System.out.println("Choose type of Real Estate. Enter 1 or 2.\n" +
                    "\t1. Apartament\n" +
                    "\t2. Building");
            String apartmentType = scanner.nextLine();
            switch (apartmentType) {
                case "1":
                    realEstate.setRealEstateType(RealEstateType.APARTMENT);
                    badType = false;
                    break;
                case "2":
                    realEstate.setRealEstateType(RealEstateType.BUILDING);
                    badType = false;
                    break;
                default:
                    System.out.println("Invalid real estate type. Try again");
            }
        } while (badType);


        System.out.print("4. Enter real estate metrics: ");
        String metrics = scanner.nextLine();
        System.out.print("5. Enter rooms: ");
        String rooms = scanner.nextLine();

        try {
            realEstate.setPrice(parseDouble(price));
            realEstate.setAvailable(true);
            realEstate.setDescription(description);
            realEstate.setMetrics(metrics);
            realEstate.setRooms(parseInt(rooms));
            realEstate.setSeller(client);
            realEstate.setAddress(address);
            REAL_ESTATE_SERVICE.create(realEstate, client.getId());
            System.out.println("\n" + "Thanks! You've successfully created a real estate! ");
        } catch (IllegalArgumentException | NullPointerException | EntityNotFoundException |
                 LinkAlreadyExistsException e) {
            System.out.println("\n" + e.getMessage());
            System.out.println("Please try again.");
        }
    }

    private static void viewRealEstateByType(Scanner scanner) {
        System.out.println("Choose what type of Real Estate you are looking for (enter 1 or 2)\n" +
                "\t1. Apartament\n" +
                "\t2. Building");
        RealEstateType realEstateType;
        String typeChoice = scanner.nextLine();
        switch (typeChoice) {
            case "1":
                realEstateType = RealEstateType.APARTMENT;
                break;
            case "2":
                realEstateType = RealEstateType.BUILDING;
                break;
            default:
                System.out.println("Invalid option.");
                return;
        }
        List<RealEstate> realEstates = REAL_ESTATE_SERVICE.getAllByType(realEstateType);
        if (realEstates.isEmpty()) {
            System.out.println("\nNo real estates by this type");
        } else {
            for (RealEstate rlSt : realEstates) {
                System.out.println(rlSt);
            }
        }
    }

    private static void viewAllRealEstates() {
        List<RealEstate> realEstates = REAL_ESTATE_SERVICE.getAll();
        for (RealEstate item : realEstates) {
            System.out.println(item + "\n");
        }
    }

    private static void deleteAccount(Scanner scanner, Client client) {
        System.out.println("Do you really want to delete your account?");
        System.out.println("Choose an action (enter 1 or 2):");
        System.out.println("1. YES");
        System.out.println("2. Exit");

        String userInput = scanner.nextLine();

        switch (userInput) {
            case "1":
                CLIENT_SERVICE.deleteById(client.getId());
                break;
            case "2":
                break;
            default:
                System.out.println("Invalid option.");
                break;
        }
    }

    private static void viewClientsMeetings(Scanner scanner, Client client) throws EntityNotFoundException {
        List<Meeting> meetings = MEETING_SERVICE.getByClient(client);
        System.out.println("All your meetings \n");
        for (Meeting meeting : meetings) {
            System.out.println(meeting);
        }
        System.out.println("Input the id of the meeting you want to change");
        String meetingId = scanner.nextLine();
        Meeting meeting = MEETING_SERVICE.getById(parseLong(meetingId));
        System.out.println("If you need, you can change the date of the meeting, or employee\n" +
                "1. Change date \n" +
                "2. Change employee\n" +
                "3. Exit");
        String choiceMeeting = scanner.nextLine();
        switch (choiceMeeting) {
            case "1":
                System.out.println("Input the new date");
                String dateString = scanner.nextLine();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date utilDate = null;
                try {
                    utilDate = dateFormat.parse(dateString);
                } catch (ParseException e) {
                    System.out.println("Enter your date in the yyyy-MM-dd format");
                }
                java.sql.Date date = new java.sql.Date(utilDate.getTime());

                meeting.setMeetingDateTime(date);
                MEETING_SERVICE.update(meeting, meeting.getRealEstate().getId(),
                        client.getId(), meeting.getEmployee().getId());
                break;
            case "2":
                Employee employee = chooseEmployee(scanner);
                meeting.setEmployee(employee);
                MEETING_SERVICE.update(meeting, meeting.getRealEstate().getId(),
                        client.getId(), meeting.getEmployee().getId());
                break;
            case "3":
                break;
            default:
                System.out.println("Invalid option");
        }
        MEETING_SERVICE.update(meeting, meeting.getRealEstate().getId(),
                client.getId(), meeting.getEmployee().getId());
    }

    public static void orderRealEstate(Scanner scanner, Client client) throws EntityNotFoundException {
        if (AGREEMENT_SERVICE.getByClientId(client.getId()).isPresent()) {
            System.out.println("You cannot have more than one Real Estate AGREEMENT open. Please pay for your agreement");
            askForPayment(scanner, client);
            return;
        }

        System.out.println("Enter the id of Real Estate you want to buy");
        String choice = scanner.nextLine();

        RealEstate realEstate;
        realEstate = REAL_ESTATE_SERVICE.getById(parseLong(choice));

        System.out.println("The price of Real Estate " + realEstate.getPrice());

        Agreement agreement = new Agreement();
        agreement.setRealEstate(realEstate);
        agreement.setDate(new Date());
        agreement.setDuration("3 months");
        agreement.setAmount(realEstate.getPrice());
        agreement.setClient(client);
        agreement.setStatus("unpaid");

        AGREEMENT_SERVICE.create(agreement, realEstate.getId(), client.getId());

        System.out.println("Your agreement is ready ");
        askForPayment(scanner, client);
        System.out.println(agreement);
    }

    private static void askForPayment(Scanner scanner, Client client) {
        System.out.println("Please pay for your agreement");
        System.out.println("Enter 1 to pay, or 0 to exit");
        String choiceToPay = scanner.nextLine();
        switch (choiceToPay) {
            case "1":
                try {
                    payForAgreement(client);
                } catch (IllegalArgumentException | NullPointerException | EntityNotFoundException e) {
                    System.out.println(e.getMessage());
                }
                break;
            case "0":
                break;
            default:
                System.out.println("Invalid option");
                break;
        }
    }

        private static void payForAgreement(Client client) throws EntityNotFoundException {
        Optional<Agreement> agreement = AGREEMENT_SERVICE.getByClientId(client.getId());

        System.out.println("Thank you for paying for agreement");
        RealEstate realEstate = agreement.get().getRealEstate();
        realEstate.setAvailable(false);
        REAL_ESTATE_SERVICE.update(realEstate);

        AGREEMENT_SERVICE.deleteById(agreement.get().getId());
        for (Meeting meetingToDelete : MEETING_SERVICE.getByRealEstate(realEstate)) {
            MEETING_SERVICE.deleteById(meetingToDelete.getId());
        }
    }

    public static void settings(Scanner scanner, Client client) {
        System.out.println("SETTINGS\n" +
                "1. Change phone  number\n" +
                "2. Change email");
        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                System.out.println("Input your new phone number");
                String phoneNumber = scanner.nextLine();
                client.setPhoneNumber(phoneNumber);
                try {
                    CLIENT_SERVICE.update(client);
                    Optional<Agreement> agreement = AGREEMENT_SERVICE.getByClientId(client.getId());
                    if (agreement.isPresent()) {
                        agreement.get().setClient(client);
                        AGREEMENT_SERVICE.update(agreement.get());
                    }
                } catch (EntityNotFoundException | EmailAlreadyExistsException | PhoneNumberAlreadyExistsException e) {
                    System.out.println(e.getMessage());
                }
                break;
            case "2":
                System.out.println("Input your new email");
                String email = scanner.nextLine();
                client.setEmail(email);
                try {
                    CLIENT_SERVICE.update(client);
                } catch (EntityNotFoundException | EmailAlreadyExistsException | PhoneNumberAlreadyExistsException e) {
                    System.out.println(e.getMessage());
                }
                break;
        }
    }

    public static void createMeeting(Scanner scanner, Client client, Employee employee) {
        Meeting meeting = new Meeting();
        RealEstate realEstate = null;

        System.out.println("Input what date you want to make a view");
        String dateString = scanner.nextLine();
        System.out.println("Enter the id of Real Estate you want to view");
        String realEstateString = scanner.nextLine();

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date utilDate = dateFormat.parse(dateString);
            java.sql.Date date = new java.sql.Date(utilDate.getTime());

            realEstate = REAL_ESTATE_SERVICE.getById(parseLong(realEstateString));

            meeting.setMeetingDateTime(date);
            meeting.setInquiryDate(new Date());
            meeting.setMeetingStatus("Pending");
            meeting.setBuyer(client);
            meeting.setRealEstate(realEstate);
            meeting.setEmployee(employee);

            MEETING_SERVICE.create(meeting, realEstate.getId(), client.getId(), employee.getId());

            System.out.println("Your meeting will be at " + date + " with " + employee.getFirstName() + " " + employee.getLastName());

        } catch (ParseException e) {
            System.out.println("Enter your date in the yyyy-MM-dd format");
        } catch (EntityNotFoundException | NullPointerException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            System.out.println("Error occurred. Meeting could not be created.");
        }
    }

    public static Employee chooseEmployee(Scanner scanner) throws EntityNotFoundException {
        System.out.println("Here the list of employees, choose the one");
        List<Employee> employees = EMPLOYEE_SERVICE.getAll();
        for (Employee empl : employees) {
            System.out.println(empl + "\n");
        }
        System.out.println("Input the id of employee ");
        String emplId = scanner.nextLine();
        return EMPLOYEE_SERVICE.getById(parseLong(emplId));
    }

    private static int parseInt(String rooms) {
        try {
            return Integer.parseInt(rooms);
        } catch (NullPointerException | NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Specified incorrect rooms amount: %s", rooms), e);
        }
    }

    private static long parseLong(String choice) {
        try {
            return Long.parseLong(choice);
        } catch (NullPointerException | NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Specified incorrect id: %s", choice), e);
        }
    }

    private static BigDecimal parseDouble(String price) {
        try {
            return BigDecimal.valueOf(Double.parseDouble(price));
        } catch (NullPointerException | NumberFormatException e) {
            throw new IllegalArgumentException(String.format("Specified incorrect price: %s", price), e);
        }
    }
    // Define other methods for handling real estate actions, employee management, agreements, etc.
}