package song.detection.dtw.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import song.detection.dtw.dto.UploadResultsDTO;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class DTWService {


    public UploadResultsDTO analyzeTracks(List<MultipartFile> uploads) {
        List<byte[]> files = uploads.stream().map(this::uploadToBytes).collect(Collectors.toList());
        // TODO: implement
        return new UploadResultsDTO();
    }


    private byte[] uploadToBytes(MultipartFile file) {
        // TODO: Do something nice, when somebody tries to break the app.
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

}
