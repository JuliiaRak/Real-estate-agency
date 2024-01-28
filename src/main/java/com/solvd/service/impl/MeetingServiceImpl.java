package com.solvd.service.impl;

import com.solvd.domain.Client;
import com.solvd.domain.Meeting;
import com.solvd.domain.exceptions.EntityNotFoundException;
import com.solvd.persistence.MeetingRepository;
import com.solvd.persistence.impl.MeetingRepositoryMybatisImpl;
import com.solvd.service.ClientService;
import com.solvd.service.EmployeeService;
import com.solvd.service.MeetingService;
import com.solvd.service.RealEstateService;
import com.solvd.service.validators.Validator;
import com.solvd.service.validators.date.FutureDateValidator;
import com.solvd.service.validators.date.NotNullDateValidator;
import com.solvd.service.validators.date.PastDateValidator;
import com.solvd.service.validators.object.NotNullObjectValidator;
import com.solvd.service.validators.string.NotEmptyStringValidator;
import com.solvd.service.validators.string.NotNullStringValidator;
import com.solvd.service.validators.string.SizeStringValidator;
import lombok.AllArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class MeetingServiceImpl implements MeetingService {

    private final MeetingRepository meetingRepository;
    private final RealEstateService realEstateService;
    private final ClientService clientService;
    private final EmployeeService employeeService;

    public MeetingServiceImpl() {
        this.meetingRepository = new MeetingRepositoryMybatisImpl();
        this.realEstateService = new RealEstateServiceImpl();
        this.clientService = new ClientServiceImpl();
        this.employeeService = new EmployeeServiceImpl();
    }

    @Override
    public void create(Meeting meeting, Long realEstateId, Long buyerId, Long employeeId) throws EntityNotFoundException {
        validate(meeting);
        checkRealEstate(realEstateId);
        checkBuyer(buyerId);
        checkEmployee(employeeId);

        meetingRepository.create(meeting, realEstateId, buyerId, employeeId);
    }

    private void checkRealEstate(Long realEstateId) throws EntityNotFoundException {
        if (!realEstateService.existsById(realEstateId)) {
            throw new EntityNotFoundException("Real estate");
        }
    }

    private void checkBuyer(Long buyerId) throws EntityNotFoundException {
        if (!clientService.existsById(buyerId)) {
            throw new EntityNotFoundException("Buyer");
        }
    }

    private void checkEmployee(Long employeeId) throws EntityNotFoundException {
        if (!employeeService.existsById(employeeId)) {
            throw new EntityNotFoundException("Employee");
        }
    }

    @Override
    public void deleteById(long id) {
        meetingRepository.deleteById(id);
    }

    @Override
    public void update(Meeting meeting, Long realEstateId, Long buyerId, Long employeeId) throws EntityNotFoundException {
        if (meetingRepository.findById(meeting.getId()).isEmpty()) {
            throw new EntityNotFoundException("Meeting", meeting.getId());
        }
        validate(meeting);
        meetingRepository.update(meeting, realEstateId, buyerId, employeeId);
    }

    @Override
    public Meeting getById(long id) throws EntityNotFoundException {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meeting", id));
    }
    @Override
    public Meeting getByClient(Client client) throws EntityNotFoundException {
        return meetingRepository.findAll().stream()
                .filter(meeting -> meeting.getBuyer().getId() == client.getId())
                .findFirst().orElseThrow(() -> new EntityNotFoundException("meeting", client.getId()));
    }

    @Override
    public List<Meeting> getAll() {
        return meetingRepository.findAll();
    }

    private void validate(Meeting meeting) {
        Validator<Object> objectValidator = new NotNullObjectValidator();
        objectValidator.validate("meeting", meeting);

        Validator<Date> notNullDateValidator = new NotNullDateValidator();
        Validator<Date> futureDateValidator = new FutureDateValidator(notNullDateValidator);
        futureDateValidator.validate("meeting date", meeting.getMeetingDateTime());

        Validator<Date> pastDateValidator = new PastDateValidator(notNullDateValidator);
        pastDateValidator.validate("inquiry date", meeting.getInquiryDate());

        Validator<String> stringValidator = new SizeStringValidator(new NotEmptyStringValidator(new NotNullStringValidator()));
        stringValidator.validate("meeting status", meeting.getMeetingStatus());
    }
}
