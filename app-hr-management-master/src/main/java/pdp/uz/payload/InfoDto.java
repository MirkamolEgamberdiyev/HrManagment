package pdp.uz.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pdp.uz.entity.Task;
import pdp.uz.entity.TourniquetHistory;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InfoDto implements Serializable {

    private String firstName;

    private String lastName;

    private String email;

    private List<Task> tasks;

    private List<TourniquetHistory> histories;
}
