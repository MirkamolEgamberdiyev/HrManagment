package pdp.uz.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeSalaryDto implements Serializable {

    @NotNull
    private String email;

    @NotNull
    private Double salary;
}
