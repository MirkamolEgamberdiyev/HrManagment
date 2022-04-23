package pdp.uz.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto implements Serializable {

    @NotNull
    private String name;

    @NotNull
    private String description;

    @NotNull
    private Integer deadlineDay;

    @NotNull
    private String email;
}
