package pdp.uz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pdp.uz.entity.Employee;
import pdp.uz.entity.Salary;
import pdp.uz.entity.Task;
import pdp.uz.entity.TourniquetHistory;
import pdp.uz.entity.enums.TaskStatus;
import pdp.uz.payload.*;
import pdp.uz.repository.EmployeeRepository;
import pdp.uz.repository.SalaryRepository;
import pdp.uz.repository.TaskRepository;
import pdp.uz.repository.TourniquetHistoryRepository;
import pdp.uz.security.JwtFilter;
import pdp.uz.security.JwtProvider;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmployeeService implements UserDetailsService {

    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    TourniquetHistoryRepository tourniquetHistoryRepository;
    @Autowired
    SalaryRepository salaryRepository;
    @Autowired
    HireService hireService;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtProvider jwtProvider;
    @Autowired
    JwtFilter jwtFilter;


    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return employeeRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(email + " not found"));
    }


    public ApiResponse login(LoginDto loginDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
            Employee employee = (Employee) authentication.getPrincipal();

            String token = jwtProvider.generateToken(employee.getEmail(), employee.getRoles());

            List<Task> tasks =
                    taskRepository.findAllByDeadlineBeforeAndEmployee_EmailAndTaskStatus(Date.valueOf(LocalDate.now()), employee.getEmail(), TaskStatus.WORKING);
            for (Task task : tasks) {
                task.setTaskStatus(TaskStatus.UNCOMPLETED);
                taskRepository.save(task);
            }
            tasks =
                    taskRepository.findAllByDeadlineBeforeAndEmployee_EmailAndTaskStatus(Date.valueOf(LocalDate.now()), employee.getEmail(), TaskStatus.NEW);
            for (Task task : tasks) {
                task.setTaskStatus(TaskStatus.UNCOMPLETED);
                taskRepository.save(task);
            }
            return new ApiResponse("Token", true, token);
        } catch (BadCredentialsException e) {
            return new ApiResponse("Login or password is incorrect", false);
        }
    }

    public ApiResponse get() {
        Employee employee = getEmployee();
        if (employee == null) {
            return new ApiResponse("Error", false);
        }
        List<Employee> employees = employeeRepository.findAllByCompanyIdAndEnabledTrue(employee.getCompany().getId());
        return new ApiResponse("Completed", true, employees);
    }

    public Employee getEmployee() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {
            return (Employee) authentication.getPrincipal();
        }
        return null;
    }

    public ApiResponse edit(EmployeeDto email, String id) {
        Optional<Employee> optionalEmployee = employeeRepository.findById(UUID.fromString(id));
        if (!optionalEmployee.isPresent()) {
            return new ApiResponse("Employee not found", false);
        }
        Employee employee = optionalEmployee.get();
        employee.setEmail(email.getEmail());
        employee.setEnabled(false);
        employee.setEmailCode(UUID.randomUUID().toString());
        Employee savedEmployee = employeeRepository.save(employee);
        hireService.sendEmail(savedEmployee.getEmail(), savedEmployee.getEmailCode());
        return new ApiResponse("Employee updated", true);
    }

    public ApiResponse editSalary(EmployeeSalaryDto dto) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmailAndEnabledTrue(dto.getEmail());
        if (!optionalEmployee.isPresent()) {
            return new ApiResponse("Employee not found", false);
        }
        Employee employee = optionalEmployee.get();
        employee.setSalary(dto.getSalary());
        employeeRepository.save(employee);
        return new ApiResponse("Successfully", true);
    }

    public ApiResponse info(String email, String from, String to) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmail(email);
        if (!optionalEmployee.isPresent()) {
            return new ApiResponse("Employee not found", false);
        }
        try {
            Employee employee = optionalEmployee.get();
            Timestamp fromDate = Timestamp.valueOf(Date.valueOf(from) + " 00:00:00");
            Timestamp toDate = Timestamp.valueOf(Date.valueOf(to) + " 00:00:00");

            InfoDto infoDto = new InfoDto();
            infoDto.setEmail(employee.getEmail());
            infoDto.setFirstName(employee.getFirstName());
            infoDto.setLastName(employee.getLastName());

            List<TourniquetHistory> histories = tourniquetHistoryRepository.findAllByExitedAtBetween(fromDate, toDate);
            infoDto.setHistories(histories);

            histories.addAll(tourniquetHistoryRepository.findAllByEnteredAtBetween(fromDate, toDate));

            List<Task> tasks = taskRepository.findAllByCompletedAtBetweenAndEmployee_Email(fromDate, toDate, email);
            infoDto.setTasks(tasks);

            return new ApiResponse("Success", true, infoDto);

        } catch (Exception e) {
            return new ApiResponse("Date parse exception", false);
        }
    }

    public ApiResponse infoSalary(String email, String from, String to) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmail(email);
        if (!optionalEmployee.isPresent()) {
            return new ApiResponse("Employee not found", false);
        }
        try {
            Timestamp fromDate = Timestamp.valueOf(Date.valueOf(from) + " 00:00:00");
            Timestamp toDate = Timestamp.valueOf(Date.valueOf(to) + " 00:00:00");
            List<Salary> salaries = salaryRepository.findAllByUpdatedAtBetweenAndEmployeeId(fromDate, toDate, optionalEmployee.get().getId());

            return new ApiResponse("Success", true, salaries);
        } catch (Exception e) {
            return new ApiResponse("Date parse exception", false);
        }
    }

    public ApiResponse infoTask(String email, String from, String to) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmail(email);
        if (!optionalEmployee.isPresent()) {
            return new ApiResponse("Employee not found", false);
        }
        try {
            Timestamp fromDate = Timestamp.valueOf(Date.valueOf(from) + " 00:00:00");
            Timestamp toDate = Timestamp.valueOf(Date.valueOf(to) + " 00:00:00");

            List<Task> completedTasks = taskRepository.findAllByCompletedAtBetweenAndEmployee_Email(fromDate, toDate, email);
            List<Task> uncompletedTasks
                    = taskRepository.findAllByCompletedAtBetweenAndTaskStatusAndEmployee_Email(fromDate, toDate, TaskStatus.UNCOMPLETED, email);
            completedTasks.addAll(uncompletedTasks);
            return new ApiResponse("Success", true, completedTasks);
        } catch (Exception e) {
            return new ApiResponse("Date parse exception", false);
        }
    }

    public ApiResponse delete(String email) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmail(email);
        if (!optionalEmployee.isPresent()) {
            return new ApiResponse("Employee not found", false);
        }
        Employee employee = optionalEmployee.get();
        employee.setEnabled(false);
        employeeRepository.save(employee);
        return new ApiResponse("Employee deleted", true);
    }
}
