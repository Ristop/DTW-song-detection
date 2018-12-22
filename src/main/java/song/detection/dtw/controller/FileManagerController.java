package song.detection.dtw.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import song.detection.dtw.dto.FileUploadDTO;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class FileManagerController {

    @GetMapping("/")
    public String getSongUpload(@ModelAttribute("dto") FileUploadDTO dto) {
        return "upload";
    }

    @PostMapping("/")
    public String uploadSong(@ModelAttribute("dto") @Valid FileUploadDTO dto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            // TODO:  handle invalid upload
            throw new RuntimeException();
        }


        List<byte[]> files = dto.getFiles()
                .stream()
                .map(this::uploadToBytes)
                .collect(Collectors.toList());

        log.info("Somebody uploaded {} files.", files.size());


        return "upload";
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
