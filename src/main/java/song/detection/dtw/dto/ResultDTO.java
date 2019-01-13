package song.detection.dtw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultDTO {
    private String trackName1;
    private String trackName2;
    private String dtw;
    private float[] trackFloats1;
    private float[] trackFloats2;
}
