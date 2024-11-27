package uni.backend.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

@Data
public class HomeDataResponse {

    //    String profileString;
    private List<HomeProfileResponse> data;

}
