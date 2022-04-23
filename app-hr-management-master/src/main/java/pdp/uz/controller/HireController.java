package pdp.uz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pdp.uz.payload.ApiResponse;
import pdp.uz.payload.EmployeeHireDto;
import pdp.uz.service.HireService;


import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@RequestMapping(path = "/api/hire")
public class HireController {

    @Autowired
    HireService hireService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/addDirector")
    public ResponseEntity<?> addDirector(@Valid @RequestBody EmployeeHireDto dto){
        ApiResponse apiResponse = hireService.addDirector(dto);
        return ResponseEntity.status(apiResponse.isStatus() ? 201 : 409).body(apiResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR')")
    @PostMapping("/addManager")
    public ResponseEntity<?> addManager(@Valid @RequestBody EmployeeHireDto dto) {
        ApiResponse apiResponse = hireService.addManager(dto);
        return ResponseEntity.status(apiResponse.isStatus() ? 201 : 409).body(apiResponse);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'DIRECTOR', 'HR_MANAGER')")
    @PostMapping("/addWorker")
    public ResponseEntity<?> addWorker(@Valid @RequestBody EmployeeHireDto dto) {
        ApiResponse apiResponse = hireService.addWorker(dto);
        return ResponseEntity.status(apiResponse.isStatus() ? 201 : 409).body(apiResponse);
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyPassword(@RequestParam String emailCode, @RequestParam String email, HttpServletRequest request) {
        ApiResponse apiResponse = hireService.verify(emailCode, email, request);
        return ResponseEntity.status(apiResponse.isStatus() ? 200 : 409).body(apiResponse);
    }

}
