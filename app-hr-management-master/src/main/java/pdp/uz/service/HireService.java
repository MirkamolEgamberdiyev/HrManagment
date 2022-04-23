package pdp.uz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pdp.uz.entity.Company;
import pdp.uz.entity.Employee;
import pdp.uz.entity.enums.RoleName;
import pdp.uz.payload.ApiResponse;
import pdp.uz.payload.EmployeeHireDto;
import pdp.uz.repository.CompanyRepository;
import pdp.uz.repository.EmployeeRepository;
import pdp.uz.repository.RoleRepository;

import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

@Service
public class HireService {

    @Autowired
    EmployeeRepository employeeRepository;

    @Autowired
    CompanyRepository companyRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JavaMailSender javaMailSender;



    public ApiResponse addManager(EmployeeHireDto dto) {
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            return new ApiResponse("Employee has already existed", false);
        }
        Employee employee = createEmployee(dto);
        employee.setRoles(Collections.singleton(roleRepository.findByRoleName(RoleName.ROLE_HR_MANAGER)));

        employeeRepository.save(employee);

        sendEmail(employee.getEmail(), employee.getEmailCode());
        return new ApiResponse("Employee added, please confirm from email", true);
    }

    public ApiResponse addDirector(EmployeeHireDto dto) {
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            return new ApiResponse("Employee has already existed", false);
        }
        if (!companyRepository.existsById(dto.getCompanyId())) {
            return new ApiResponse("Company not found", false);
        }
        if (companyRepository.hasDirector(dto.getCompanyId())) {
            return new ApiResponse("The company has already had director", false);
        }
        Company company = companyRepository.getById(dto.getCompanyId());

        Employee employee = createEmployee(dto);
        employee.setRoles(Collections.singleton(roleRepository.findByRoleName(RoleName.ROLE_DIRECTOR)));
        employee.setCompany(company);
        employeeRepository.save(employee);

        sendEmail(employee.getEmail(), employee.getEmailCode());
        return new ApiResponse("Employee added, please confirm from email", true);
    }

    public ApiResponse addWorker(EmployeeHireDto dto) {
        if (employeeRepository.existsByEmail(dto.getEmail()))
            return new ApiResponse("Employee has already existed", false);

        Employee employee = createEmployee(dto);
        employee.setRoles(Collections.singleton(roleRepository.findByRoleName(RoleName.ROLE_EMPLOYEE)));

        employeeRepository.save(employee);

        sendEmail(employee.getEmail(), employee.getEmailCode());
        return new ApiResponse("Employee added, please confirm from email", true);
    }

    public Employee createEmployee(EmployeeHireDto dto) {
        Employee employee = new Employee();
        employee.setEmail(dto.getEmail());
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmailCode(UUID.randomUUID().toString());
        employee.setSalary(dto.getSalary());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {
            Employee employee1 = (Employee) authentication.getPrincipal();
            employee.setCompany(employee1.getCompany());
        }
        return employee;
    }

    public void sendEmail(String sendingEmail, String emailCode) {
        String link = "http://localhost:8081/api/hire/verify?emailCode=" + emailCode + "&email=" + sendingEmail;
        String body = "<form action=" + link + " method=\"post\">\n" +
                "<label>Create password for your cabinet</label>" +
                "<br/><input type=\"text\" name=\"password\" placeholder=\"password\">\n" +
                "<br/>  <button>Submit</button>\n" +
                "</form>";
        try {
            String from = "erlantest180@gmail.com";
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setSubject("Confirm email");
            helper.setFrom(from);
            helper.setTo(sendingEmail);
            helper.setText(body, true);
            javaMailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ApiResponse verify(String emailCode, String email, HttpServletRequest request) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmailAndEmailCode(email, emailCode);
        if (!optionalEmployee.isPresent()) {
            return new ApiResponse("Email or code is not correct", false);
        }
        Employee employee = optionalEmployee.get();
        employee.setPassword(passwordEncoder.encode(request.getParameter("password")));
        employee.setEnabled(true);
        employee.setEmailCode(null);
        employeeRepository.save(employee);
        return new ApiResponse("Account registered successfully", true);
    }
}
