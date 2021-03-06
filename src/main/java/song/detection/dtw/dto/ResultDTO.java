package song.detection.dtw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultDTO {

    private List<TrackDTO> trackDTOS = new ArrayList<>();
    private List<VectorDTO> vectorDTOS = new ArrayList<>();
    private float dtw;
}
