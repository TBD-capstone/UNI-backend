package uni.backend.domain.dto;

import java.util.List;
import lombok.Data;

@Data
public class GlossariesListResponse {

    List<GlossaryResponse> glossaries;
}
