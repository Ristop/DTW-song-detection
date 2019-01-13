package song.detection.dtw.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VectorDTO {

    private String name;
    private List<PointDTO> data = new ArrayList<>();
    private String color;
    private boolean showInLegend;

}
