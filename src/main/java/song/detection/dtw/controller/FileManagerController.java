package song.detection.dtw.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import song.detection.dtw.dto.FileUploadDTO;
import song.detection.dtw.dto.ResultDTO;
import song.detection.dtw.service.DTWService;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class FileManagerController {
    private final DTWService dtwService;

    @GetMapping("/")
    public String getSongUpload(@ModelAttribute("dto") FileUploadDTO dto) {
        return "upload";
    }

    @PostMapping("/")
    public String uploadSong(@ModelAttribute("dto") @Valid FileUploadDTO dto, BindingResult bindingResult, Model model) {

        if (bindingResult.hasErrors()) {
            // TODO:  handle invalid upload
            throw new RuntimeException();
            // return "upload";
        }

        log.info("Somebody uploaded {} files.", dto.getFiles().size());

        ResultDTO results = dtwService.analyzeTracks(dto.getFiles());
        model.addAttribute("results", results);

        return "results";
    }
}
