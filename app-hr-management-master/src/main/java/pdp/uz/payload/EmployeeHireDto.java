package pdp.uz.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeHireDto implements Serializable {

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private Double salary;

    @NotNull
    @Email
    private String email;

    private Long companyId;
}
