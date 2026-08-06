package iteration2_middle.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferFundsRequest extends BaseModel {
    int senderAccountId;
    int receiverAccountId;
    double amount;
}
