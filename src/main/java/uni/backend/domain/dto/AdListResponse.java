package uni.backend.domain.dto;

import java.util.List;
import lombok.Data;
import uni.backend.domain.Ad;

@Data
public class AdListResponse {

    List<AdResponse> ads;
}
