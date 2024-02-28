package nextstep.favorite.application;

import nextstep.favorite.application.dto.FavoriteRequest;
import nextstep.favorite.application.dto.FavoriteResponse;
import nextstep.favorite.domain.Favorite;
import nextstep.favorite.domain.FavoriteRepository;
import nextstep.member.AuthenticationException;
import nextstep.member.application.MemberService;
import nextstep.member.domain.LoginMember;
import nextstep.member.domain.Member;
import nextstep.subway.domain.Line;
import nextstep.subway.domain.PathFinder;
import nextstep.subway.domain.Station;
import nextstep.subway.domain.repository.LineRepository;
import nextstep.subway.service.StationService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static nextstep.subway.controller.dto.StationResponse.stationToStationResponse;

@Service
public class FavoriteService {
    private FavoriteRepository favoriteRepository;
    private MemberService memberService;
    private StationService stationService;
    private LineRepository lineRepository;

    public FavoriteService(FavoriteRepository favoriteRepository, MemberService memberService, StationService stationService, LineRepository lineRepository) {
        this.favoriteRepository = favoriteRepository;
        this.memberService = memberService;
        this.stationService = stationService;
        this.lineRepository = lineRepository;
    }

    public Long createFavorite(LoginMember loginMember, FavoriteRequest request) {
        Member member = memberService.getMemberByEmailOrThrow(loginMember.getEmail());

        Station sourceStation = stationService.getStationById(request.getSource());
        Station targetStation = stationService.getStationById(request.getTarget());

        validFindPath(lineRepository.findAll(), sourceStation, targetStation);

        Favorite favorite = Favorite.builder()
                .sourceStation(sourceStation)
                .targetStation(targetStation)
                .member(member)
                .build();
        member.addFavorite(favorite);

        return favoriteRepository.save(favorite).getId();
    }

    public List<FavoriteResponse> findFavorites(LoginMember loginMember) {
        memberService.getMemberByEmailOrThrow(loginMember.getEmail());

        List<Favorite> favorites = favoriteRepository.findAll();
        return favorites.stream().map(favorite -> FavoriteResponse.builder()
                .id(favorite.getId())
                .source(stationToStationResponse(favorite.getSourceStation()))
                .target(stationToStationResponse(favorite.getTargetStation()))
                .build()).collect(Collectors.toList());
    }

    /**
     * TODO: 요구사항 설명에 맞게 수정합니다.
     *
     * @param id
     */
    public void deleteFavorite(Long id) {
        favoriteRepository.deleteById(id);
    }

    private void validFindPath(List<Line> lineList, Station source, Station target) {
        PathFinder pathFinder = new PathFinder();
        pathFinder.findPath(lineList, source, target);
    }
}
