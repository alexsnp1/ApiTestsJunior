package iteration2_middle.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminCreateUserRequest extends BaseModel {
    private String username;
    private String password;
    private String role;
}
