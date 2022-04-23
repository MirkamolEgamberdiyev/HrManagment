package pdp.uz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pdp.uz.payload.ApiResponse;
import pdp.uz.payload.TaskDto;
import pdp.uz.service.TaskService;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    @Autowired
    TaskService taskService;

    @PreAuthorize("hasRole('DIRECTOR')")
    @PostMapping("/manager")
    public ResponseEntity<?> createManager(@Valid @RequestBody TaskDto taskDto){
        ApiResponse apiResponse = taskService.createManager(taskDto);
        return ResponseEntity.status(apiResponse.isStatus() ? 201 : 409).body(apiResponse);
    }

    @PreAuthorize("hasAnyRole('DIRECTOR', 'HR_MANAGER')")
    @PostMapping("/worker")
    public ResponseEntity<?> createForWorker(@Valid @RequestBody TaskDto dto){
        ApiResponse apiResponse = taskService.createForWorker(dto);
        return ResponseEntity.status(apiResponse.isStatus() ? 201 : 409).body(apiResponse);
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestParam String email, @RequestParam String taskCode){
        ApiResponse apiResponse = taskService.confirm(email, taskCode);
        return ResponseEntity.status(apiResponse.isStatus() ? 200 : 401).body(apiResponse);
    }

    @PreAuthorize("hasAnyRole('DIRECTOR', 'HR_MANAGER', 'WORKER')")
    @PostMapping("/completed/{taskCode}")
    public ResponseEntity<?> completed(@PathVariable String taskCode){
        ApiResponse apiResponse = taskService.completed(taskCode);
        return ResponseEntity.status(apiResponse.isStatus() ? 200 : 409).body(apiResponse);
    }

    @PreAuthorize("hasRole('DIRECTOR')")
    @DeleteMapping("/{taskCode}")
    public ResponseEntity<?> delete(@PathVariable String taskCode) {
        ApiResponse apiResponse = taskService.delete(taskCode);
        return ResponseEntity.status(apiResponse.isStatus() ? 200 : 401).body(apiResponse);
    }
}
