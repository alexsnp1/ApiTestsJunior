package iteration2_senior.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferFundsResponse extends BaseModel{
    private String message;
    private double amount;
    private int receiverAccountId;
    private int senderAccountId;
}
