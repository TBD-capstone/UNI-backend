package uni.backend.domain.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;
import uni.backend.domain.Ad;

@Builder
@Data
public class AdListResponse {

    List<AdResponse> ads;
}
