package teamproject.pocoapoco.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import teamproject.pocoapoco.domain.entity.Crew;
import teamproject.pocoapoco.enums.SportTest;

public interface CrewRepository extends JpaRepository<Crew, Long> {

    //지역 검색 By String
    Page<Crew> findByStrictContaining(Pageable pageable, String strict);


    //운동 검색 By String
    Page<Crew> findBySprotStrContaining(Pageable pageable, String sport);


    //운동 다중 검색 By String
    Page<Crew> findBySprotStrOrSprotStrOrSprotStr(Pageable pageable, String sport, String sport2, String sport3);

    //운동 다중검색2 by String
    @Query("select s from Crew s where s.sprotStr=:sport or s.sprotStr=:sport2 or s.sprotStr=:sport3")
    Page<Crew> findBySprotStr(Pageable pageable, String sport, String sport2, String sport3);


    //운동 검색 By Enum
    Page<Crew> findBySportTest(Pageable pageable, SportTest test);


    //    @Query("select s from Crew s where s.sport.soccer=true")
    //    Page<Crew> findBySportSoccer(Pageable pageable, boolean sport);

}
