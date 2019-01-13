package song.detection.dtw.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import song.detection.dtw.dto.VectorDTO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DTWResult {

    private float distance;
    private List<VectorDTO> vectorDTOList;

}
