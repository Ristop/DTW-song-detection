package song.detection.dtw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrackDTO {

    private String name;
    private float[] data;
    private String color;

}
