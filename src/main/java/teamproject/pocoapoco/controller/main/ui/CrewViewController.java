package teamproject.pocoapoco.controller.main.ui;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import teamproject.pocoapoco.domain.dto.Review.ReviewRequest;
import teamproject.pocoapoco.domain.dto.Review.ReviewResponse;
import teamproject.pocoapoco.domain.dto.crew.CrewDetailResponse;
import teamproject.pocoapoco.domain.dto.crew.CrewRequest;
import teamproject.pocoapoco.domain.dto.crew.CrewResponse;
import teamproject.pocoapoco.domain.dto.crew.CrewSportRequest;
import teamproject.pocoapoco.domain.dto.crew.review.CrewReviewDetailResponse;
import teamproject.pocoapoco.domain.dto.crew.review.CrewReviewResponse;
import teamproject.pocoapoco.domain.dto.like.LikeViewResponse;
import teamproject.pocoapoco.domain.entity.Crew;
import teamproject.pocoapoco.domain.entity.User;
import teamproject.pocoapoco.enums.SportEnum;
import teamproject.pocoapoco.repository.CrewRepository;
import teamproject.pocoapoco.repository.UserRepository;
import teamproject.pocoapoco.service.CrewReviewService;
import teamproject.pocoapoco.service.CrewService;
import teamproject.pocoapoco.service.LikeViewService;
import teamproject.pocoapoco.service.part.ParticipationService;

import javax.transaction.Transactional;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/view/v1/crews")
@Slf4j
@Transactional
@Api(tags = {"Crew Controller"})
public class CrewViewController {

    private final CrewService crewService;
    private final LikeViewService likeViewService;
    private final CrewRepository crewRepository;
    private final UserRepository userRepository;
    private final CrewReviewService crewReviewService;

    private final ParticipationService participationService;
    /*@ModelAttribute("reviews")
    public Map<String, String> reviews() {
        Map<String, String> reviews = new LinkedHashMap<>();
        reviews.put("01", "????????? ??? ?????????.");
        reviews.put("02", "?????? ??????????????? ???????????? ?????????.");
        return reviews;
    }*/



    @Value("${aws.access.key}")
    String AWS_ACCESS_KEY;

    @Value("${aws.secret.access.key}")
    String AWS_SECRET_ACCESS_KEY;

    @Value("${aws.region}")
    String AWS_REGION;

    @Value("${aws.bucket.name}")
    String AWS_BUCKET_NAME;

    String AWS_BUCKET_DIRECTORY = "/crewimages";

    // ?????? ????????? ?????? ?????????
    @GetMapping("/{crewId}")
    public String detailCrew(@PathVariable Long crewId, Model model, @ModelAttribute("sportRequest") CrewSportRequest crewSportRequest,
                             @PageableDefault(page = 0, size = 1, sort = "lastModifiedAt", direction = Sort.Direction.DESC) Pageable pageable, Authentication authentication) {

        model.addAttribute("AWS_ACCESS_KEY", AWS_ACCESS_KEY);
        model.addAttribute("AWS_SECRET_ACCESS_KEY", AWS_SECRET_ACCESS_KEY);
        model.addAttribute("AWS_REGION", AWS_REGION);
        model.addAttribute("AWS_BUCKET_NAME", AWS_BUCKET_NAME);
        model.addAttribute("AWS_BUCKET_DIRECTORY", AWS_BUCKET_DIRECTORY);

        // ?????? ????????? ?????? ??????(????????????, ????????????, ???????????? ??????)
        Page<CrewDetailResponse> list = crewService.findAllCrewsByStrictAndSportEnum2(crewSportRequest, true, pageable);
//        if(crewId == null)
//            crewId = list.getContent().get(0).getId();
        log.info("!!!!!!!!!!!!!! crewId : {}", crewId);

        // ????????? ?????? ??????
        List<ReviewResponse> members = participationService.findAllPartMember(crewId);
        model.addAttribute("members", members);
        ReviewRequest crewReviewRequest = new ReviewRequest();
        model.addAttribute("reviewRequest", crewReviewRequest);

        try {
            //?????? ??????
            if(authentication != null) crewService.readAlarms(crewId, authentication.getName());

            // ?????????
            int count = likeViewService.getLikeCrew(crewId);
            model.addAttribute("likeCnt", count);

            // ??????????????? ??????
            CrewDetailResponse details = list.getContent().get(0);
            model.addAttribute("details", details);

            // ?????? ???????????? ??????
            if (authentication != null) {

                User nowUser = crewService.findByUserName(authentication.getName());
                boolean userReviewed = crewReviewService.findReviewedUser(crewId, nowUser);
                model.addAttribute("userReviewed", userReviewed);

                // ????????? ??????
                boolean isPartUser = participationService.isPartUser(crewId, nowUser);
                model.addAttribute("isPartUser", isPartUser);

            }

        } catch (Exception e) {
            return "redirect:/index";
        }

        // ????????? ?????? ??????
        int nowPage = list.getPageable().getPageNumber();
        int lastPage = list.getTotalPages() - 1;

        // ????????? ?????? ??????
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("lastPage", lastPage);

        return "crew/read-crew";
    }


    @GetMapping("/detail/{crewId}")
    public String detailCrew2(@PathVariable Long crewId, Model model, Authentication authentication) {

        model.addAttribute("AWS_ACCESS_KEY", AWS_ACCESS_KEY);
        model.addAttribute("AWS_SECRET_ACCESS_KEY", AWS_SECRET_ACCESS_KEY);
        model.addAttribute("AWS_REGION", AWS_REGION);
        model.addAttribute("AWS_BUCKET_NAME", AWS_BUCKET_NAME);
        model.addAttribute("AWS_BUCKET_DIRECTORY", AWS_BUCKET_DIRECTORY);

        // ?????? ????????? ??????
        Crew crew = crewService.findByCrewId(crewId);

        // ????????? ?????? ??????
        List<ReviewResponse> members = participationService.findAllPartMember(crewId);
        model.addAttribute("members", members);
        ReviewRequest crewReviewRequest = new ReviewRequest();
        model.addAttribute("reviewRequest", crewReviewRequest);

        try {
            //?????? ??????
            if(authentication != null) crewService.readAlarms(crewId, authentication.getName());

            // ?????????
            int count = likeViewService.getLikeCrew(crewId);
            model.addAttribute("likeCnt", count);

            // ??????????????? ??????
            CrewDetailResponse details = CrewDetailResponse.of(crew);
            model.addAttribute("details", details);


            // ?????? ???????????? ??????
            User nowUser = crewService.findByUserName(authentication.getName());
            boolean userReviewed = crewReviewService.findReviewedUser(crewId, nowUser);
            model.addAttribute("userReviewed", userReviewed);

            // ????????? ??????
            boolean isPartUser = participationService.isPartUser(crewId, nowUser);
            model.addAttribute("isPartUser", isPartUser);


        } catch (Exception e) {
            return "redirect:/index";
        }


        return "crew/read-crew2";
    }

    // ?????? ????????? ??????
    @PutMapping("/{crewId}")
    public String modifyCrew(@PathVariable Long crewId, @ModelAttribute CrewRequest crewRequest, Authentication authentication) {
        if(authentication == null){
            log.error("null error");
            return "redirect:/";
        }
        crewService.modifyCrew(crewId, crewRequest, authentication.getName());
        return "redirect:/view/v1/crews/{crewId}";
    }

    // ?????? ????????? ????????????
    @Transactional
    @GetMapping("/update/{crewId}")
    public String updateCrew(@PathVariable Long crewId, Model model, Authentication authentication) {
        Crew crew = crewRepository.findById(crewId).orElse(null);
        if (crew == null || !crew.getUser().getUsername().equals(authentication.getName())) {
            return "error/404";
        }
        CrewRequest crewRequest = new CrewRequest();
        crewRequest.setTitle(crew.getTitle());
        crewRequest.setContent(crew.getContent());

        model.addAttribute(crewRequest);
        model.addAttribute("crewId", crew.getId());

        return "crew/update-crew";
    }

    // ?????? ????????? ??????
    @DeleteMapping("/{crewId}")
    public String deleteCrew(@PathVariable Long crewId, Model model, Authentication authentication) {
        Crew crew = crewRepository.findById(crewId).orElse(null);
        User user = userRepository.findById(crew.getUser().getId()).orElse(null);
        log.info("?????? ?????? ???");

        if (crew == null || user == null) {
            return "error/404";
        }
        CrewResponse crewResponse = crewService.deleteCrew(crewId, authentication.getName());
        model.addAttribute("response", crewResponse);
        return "redirect:/";
    }

    // ?????? ????????? ?????????
    @PostMapping("/{crewId}/like")
    public ResponseEntity likeCrew(@PathVariable Long crewId, Authentication authentication) {
        LikeViewResponse likeViewResponse = likeViewService.pressLike(crewId, authentication.getName());
        return new ResponseEntity<>(likeViewResponse, HttpStatus.OK);
    }

    // ?????? ????????? ?????? ??????, ?????? ??????, ?????? ?????? ??????
    @GetMapping()
    @ApiOperation(value = "?????? ????????? ????????????", notes = "")
    public String findAllCrew(Model model, Authentication authentication,
                              @ModelAttribute("sportRequest") CrewSportRequest crewSportRequest,
                              @PageableDefault(page = 0, size = 9, sort = "lastModifiedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        model.addAttribute("AWS_ACCESS_KEY", AWS_ACCESS_KEY);
        model.addAttribute("AWS_SECRET_ACCESS_KEY", AWS_SECRET_ACCESS_KEY);
        model.addAttribute("AWS_REGION", AWS_REGION);
        model.addAttribute("AWS_BUCKET_NAME", AWS_BUCKET_NAME);
        model.addAttribute("AWS_BUCKET_DIRECTORY", AWS_BUCKET_DIRECTORY);


        // ?????? ????????? ?????? ??? ?????? ?????? ????????? ??????
        List<String> userSportsList = crewService.getUserSports(authentication, CollectionUtils.isEmpty(crewSportRequest.getSportsList()));

        // ?????? ????????? ???, My???????????? ?????? ??? ?????? ?????? Model??? ??????
        if (!CollectionUtils.isEmpty(userSportsList) && !crewSportRequest.isLoginStatus()) {
            crewSportRequest.setSportsList(userSportsList);
            crewSportRequest.setLoginStatus(true);
        }

        // ?????? ????????? ?????? ??????
        if(crewSportRequest.getStrict() == null){
            crewSportRequest.setStrict(crewService.getUserStrict(authentication));
            log.info("!!!!!!!!!!!strict : {}", crewSportRequest.getStrict());
        }
        else{
            log.info("!!!!!!!!!!!strict : not empty");
        }

        // ?????? ????????? ?????? ??????(????????????, ????????????, ???????????? ??????)
        Page<CrewDetailResponse> list = crewService.findAllCrewsByStrictAndSportEnum2(crewSportRequest, CollectionUtils.isEmpty(userSportsList), pageable);

        // ????????? ?????? ??????
        int nowPage = list.getPageable().getPageNumber() + 1;
        int startNumPage = Math.max(nowPage - 4, 1);
        int endNumPage = Math.min(nowPage + 5, list.getTotalPages());
        int lastPage = list.getTotalPages();

        // ????????? ?????????
        model.addAttribute("crewList", list);

        // ????????? ?????? ??????
        model.addAttribute("nowPage", nowPage);
        model.addAttribute("startNumPage", startNumPage);
        model.addAttribute("endNumPage", endNumPage);
        model.addAttribute("lastPage", lastPage);

        // ?????? ?????? Enum?????????
        List<SportEnum> sportEnums = List.of(SportEnum.values());
        model.addAttribute("sportEnums", sportEnums);

        model.addAttribute("isLoginStatus", crewSportRequest.isLoginStatus());

        return "main/main";
    }

    // ?????? ?????? ??????
    @Transactional
    @GetMapping("/review/{crewId}")
    public String reviewCrew(@PathVariable Long crewId, Authentication authentication, Model model) {

        if(authentication == null){
            log.error("null pointer Error");
            return "redirect:/";
        }

        //?????? ?????? ??????
        User nowUser = crewService.findByUserName(authentication.getName());
        model.addAttribute("nowUser", nowUser.getId());

        // ?????? ????????? ??????
        Crew crew = crewService.findByCrewId(crewId);
        model.addAttribute("crew", CrewDetailResponse.of(crew));

        // ????????? ?????? ??????
        List<ReviewResponse> members = participationService.findAllPartMember(crewId);
        model.addAttribute("members", members);

        if(crewReviewService.isContainReview(crew,nowUser)){
            return "redirect:/";
        }


        ReviewRequest crewReviewRequest = new ReviewRequest();
        model.addAttribute("reviewRequest", crewReviewRequest);

        return "crew/review-crew";
    }

    // ?????? ?????? ??????
    @PostMapping("/review")
    public String reviewCrew(Model model, @ModelAttribute("reviewRequest") ReviewRequest crewReviewRequest) {
        crewReviewService.addReview(crewReviewRequest);
        return "redirect:/";
    }

    // ?????? ?????????
    @GetMapping("/{userName}/reviewList")
    public String findReviewList(@PathVariable String userName, Model model, @PageableDefault(page = 0, size = 5) @SortDefault.SortDefaults({
            @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC)}) Pageable pageable) {

        model.addAttribute("AWS_ACCESS_KEY", AWS_ACCESS_KEY);
        model.addAttribute("AWS_SECRET_ACCESS_KEY", AWS_SECRET_ACCESS_KEY);
        model.addAttribute("AWS_REGION", AWS_REGION);
        model.addAttribute("AWS_BUCKET_NAME", AWS_BUCKET_NAME);
        model.addAttribute("AWS_BUCKET_DIRECTORY", "/profileimages");


        Page<CrewReviewResponse> reviewList = crewReviewService.findAllReviewList(userName, pageable);
        model.addAttribute("reviewList", reviewList);

        // paging
        int startPage = Math.max(1,reviewList.getPageable().getPageNumber() - 4);
        int endPage = Math.min(reviewList.getTotalPages(),reviewList.getPageable().getPageNumber() + 4);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);

        long reviewAllCount = crewReviewService.getReviewAllCount(userName);
        model.addAttribute("reviewAllCount", reviewAllCount);


        return "review/review-list";
    }

    // ?????? detail
    @GetMapping("/{userName}/reviewList/{reviewId}")
    public String findReview(@PathVariable String userName, @PathVariable Long reviewId, Model model, Authentication authentication) {

        if(authentication == null){
            log.error("null pointer Error");
            return "redirect:/";
        }

        //?????? ??????
        if(authentication != null) crewService.readAlarmsReview(reviewId, authentication.getName());

        CrewReviewDetailResponse review = crewReviewService.findReviewById(reviewId);
        model.addAttribute("review", review);
        model.addAttribute("userName",userName);
        return "review/review-content";
    }

    @ModelAttribute("sportEnums")
    private List<SportEnum> sportEnums() {
        List<SportEnum> sportEnums = List.of(SportEnum.values());
        return sportEnums;
    }


    // ?????? ???????????? ?????? ?????????
    @GetMapping("/{userName}/active")
    public String getActiveCrewList(@PathVariable String userName, Model model, @PageableDefault(page = 0, size = 5) @SortDefault.SortDefaults({
                                            @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC)}) Pageable pageable) {

        model.addAttribute("AWS_ACCESS_KEY", AWS_ACCESS_KEY);
        model.addAttribute("AWS_SECRET_ACCESS_KEY", AWS_SECRET_ACCESS_KEY);
        model.addAttribute("AWS_REGION", AWS_REGION);
        model.addAttribute("AWS_BUCKET_NAME", AWS_BUCKET_NAME);
        model.addAttribute("AWS_BUCKET_DIRECTORY", AWS_BUCKET_DIRECTORY);



        // list
        Page<CrewDetailResponse> crewList = crewService.findAllCrew(2,userName, pageable); // 2: ?????? ??????
        model.addAttribute("crewList",crewList);
        // paging
        int startPage = Math.max(1,crewList.getPageable().getPageNumber() - 4);
        int endPage = Math.min(crewList.getTotalPages(),crewList.getPageable().getPageNumber() + 4);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        // count
        putCategorizeCrewCount(userName,model);
        return "part/get-current-crew";
    }

    // ????????? ?????? ?????????
    @GetMapping("/{userName}/end")
    public String getEndCrewList(@PathVariable String userName, Model model, @PageableDefault(page = 0, size = 5) @SortDefault.SortDefaults({
            @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC)}) Pageable pageable) {

        model.addAttribute("AWS_ACCESS_KEY", AWS_ACCESS_KEY);
        model.addAttribute("AWS_SECRET_ACCESS_KEY", AWS_SECRET_ACCESS_KEY);
        model.addAttribute("AWS_REGION", AWS_REGION);
        model.addAttribute("AWS_BUCKET_NAME", AWS_BUCKET_NAME);
        model.addAttribute("AWS_BUCKET_DIRECTORY", AWS_BUCKET_DIRECTORY);


        // list
        Page<CrewDetailResponse> crewList = crewService.findAllCrew(3, userName, pageable); // 3: ?????? ??????
        model.addAttribute("crewList",crewList);
        // paging
        int startPage = Math.max(1,crewList.getPageable().getPageNumber() - 4);
        int endPage = Math.min(crewList.getTotalPages(),crewList.getPageable().getPageNumber() + 4);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        // count
        putCategorizeCrewCount(userName,model);
        model.addAttribute("userName",userName);

        return "part/get-end-crew";
    }

    // ?????? crew??? count?????? ?????????
    private void putCategorizeCrewCount(String userName, Model model) {
        long activeCrewCount = crewService.getCrewByUserAndStatus(2,userName);
        long endCrewCount = crewService.getCrewByUserAndStatus(3,userName);
        model.addAttribute("activeCrewCount", activeCrewCount);
        model.addAttribute("endCrewCount", endCrewCount);

    }


}