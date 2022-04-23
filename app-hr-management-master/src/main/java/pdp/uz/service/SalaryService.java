package pdp.uz.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pdp.uz.entity.Employee;
import pdp.uz.entity.Salary;
import pdp.uz.payload.ApiResponse;
import pdp.uz.payload.SalaryDto;
import pdp.uz.repository.EmployeeRepository;
import pdp.uz.repository.SalaryRepository;

import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SalaryService {

    @Autowired
    EmployeeRepository employeeRepository;
    @Autowired
    JavaMailSender javaMailSender;
    @Autowired
    SalaryRepository salaryRepository;

    public ApiResponse pay(SalaryDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser")) {
            Employee employee = (Employee) authentication.getPrincipal();
            String verifyingCode = UUID.randomUUID().toString();
            List<Employee> employees = employeeRepository.findAllByCompanyIdAndEnabledTrue(employee.getCompany().getId());
            for (Employee employee1 : employees) {
                Salary employeeSalary = new Salary();
                employeeSalary.setSalary(employee1.getSalary());
                employeeSalary.setEmployee(employee1);
                employeeSalary.setMonth(dto.getMonth());
                employeeSalary.setVerifyingCode(verifyingCode);
                salaryRepository.save(employeeSalary);
            }
            sendEmailForSalary(employee, employees, employee.getCompany().getId(), verifyingCode, dto.getMonth());
            return new ApiResponse("Information are sent to director to confirm", true);
        }
        return new ApiResponse("Error with authentication", false);
    }


    private void sendEmailForSalary(Employee from, List<Employee> employees, Long id, String verifyingCode, String month) {
        Optional<Employee> optionalDirector = employeeRepository.findCompanyDirector(id);
        if (optionalDirector.isPresent()) {
            Employee director = optionalDirector.get();
            director.setEmailCode(verifyingCode);
            Employee savedDirector = employeeRepository.save(director);
            String confirmLink = "http://localhost:8081/api/salary/confirm?email=" + savedDirector.getEmail() + "&emailCode=" +
                    savedDirector.getEmailCode();
            String rejectLink = "http://localhost:8081/api/salary/reject?email=" + savedDirector.getEmail() + "&emailCode=" +
                    savedDirector.getEmailCode();

            String startHtml = "<table border=\"1px\" cellspacing=\"0px\" cellpadding=\"1px\">\n" +
                    "    <tr>\n" +
                    "        <th>Firstname</th>\n" +
                    "        <th>Lastname</th>\n" +
                    "        <th width=\"90px\">Salary</th>\n" +
                    "    </tr>\n";
            StringBuilder body = new StringBuilder();
            for (Employee employee : employees) {
                body.append("<tr>\n" + "<td>").append(employee.getFirstName())
                        .append("</td>\n")
                        .append("<td>")
                        .append(employee.getLastName())
                        .append("</td>\n").append("<td>")
                        .append(employee.getSalary())
                        .append("</td>\n").append("</tr>\n");
            }
            startHtml += body + "</table>\n" +
                    "<form method=\"post\" action=" + rejectLink + "> " +
                    "<button style=\"padding: 5px 10px; background-color: red; margin-top: 5px; color: white \">Reject</button></form>"
                    +"<form method=\"post\" action=" + confirmLink + "> " +
                    "<button style=\"padding: 5px 10px; background-color: #24d024; margin-top: 5px; color: white \">Confirm</button></form>";
            try {
                MimeMessage message = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message);
                helper.setSubject("Confirm salary for " + month);
                helper.setFrom(from.getEmail());
                helper.setTo(savedDirector.getEmail());
                helper.setText(startHtml, true);
                javaMailSender.send(message);
            } catch (Exception ignored) {
            }
        }
    }

    public ApiResponse confirm(String email, String emailCode) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmailAndEmailCode(email, emailCode);
        if (!optionalEmployee.isPresent()) {
            return new ApiResponse("Email or code isn't correct", false);
        }
        Employee director = optionalEmployee.get();

        if (employeeRepository.isDirector(director.getEmail())) {
            List<Salary> salaries = salaryRepository.findAllByVerifyingCode(emailCode);
            for (Salary salary : salaries) {
                salary.setStatus(true);
                salaryRepository.save(salary);
            }
            director.setEmailCode(null);
            employeeRepository.save(director);
            return new ApiResponse("All salaries are confirmed", true);
        }
        return new ApiResponse("You aren't director", false);
    }

    public ApiResponse reject(String email, String emailCode) {
        Optional<Employee> optionalEmployee = employeeRepository.findByEmailAndEmailCode(email, emailCode);
        if (!optionalEmployee.isPresent()) {
            return new ApiResponse("Email or code isn't correct", false);
        }
        Employee director = optionalEmployee.get();

        if (employeeRepository.isDirector(director.getEmail())) {
            List<Salary> salaries = salaryRepository.findAllByVerifyingCode(emailCode);
            salaryRepository.deleteAll(salaries);
            director.setEmailCode(null);
            employeeRepository.save(director);
            return new ApiResponse("All salaries are rejected", true);
        }
        return new ApiResponse("You aren't director", false);
    }
}
