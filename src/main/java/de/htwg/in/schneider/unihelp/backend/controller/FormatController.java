package de.htwg.in.schneider.unihelp.backend.controller;

import org.springframework.web.bind.annotation.*;
import de.htwg.in.schneider.unihelp.backend.model.Format;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/format")
public class FormatController {

    @GetMapping()
    public List<Format> getFormats() {
        return Arrays.asList(Format.values());
    }

    @GetMapping("/translation")
    public Map<String, String> getAllFormats() {
        return Arrays.stream(Format.values())
                .collect(Collectors.toMap(Format::name, Format::getDisplayName));
    }
}
