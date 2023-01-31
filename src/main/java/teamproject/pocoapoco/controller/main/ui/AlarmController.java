package teamproject.pocoapoco.controller.main.ui;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import teamproject.pocoapoco.domain.dto.alarm.AlarmResponse;
import teamproject.pocoapoco.service.AlarmService;

@Controller
@RequestMapping("/view/v1")
@RequiredArgsConstructor
@Slf4j
public class AlarmController {

    private final AlarmService alarmService;

    @GetMapping("/alarm")
    public String alarm(Model model, Pageable pageable, Authentication authentication){
        Page<AlarmResponse> alarms = alarmService.getAlarms(pageable, authentication.getName());
        model.addAttribute("alarms", alarms);
        return "alarms/alarm";
    }
}
