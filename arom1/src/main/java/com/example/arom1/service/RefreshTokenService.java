package com.example.arom1.service;

import com.example.arom1.common.exception.BaseException;
import com.example.arom1.common.response.BaseResponseStatus;
import com.example.arom1.common.util.jwt.TokenProvider;
import com.example.arom1.dto.response.LoginResponse;
import com.example.arom1.entity.security.MemberDetail;
import com.example.arom1.entity.security.RefreshToken;
import com.example.arom1.repository.RefreshTokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    //OAuth2에도 쓰이는 로직이므로 login 메서드와 분리
    @Transactional
    public LoginResponse buildLoginResponse(MemberDetail memberDetail) {
        //MemberDetail memberDetail1 = (MemberDetail) authentication.getPrincipal();
        Long memberId = memberDetail.getId();

        RefreshToken refreshToken = RefreshToken.of(tokenProvider.generateRefreshToken(memberDetail), memberId);
        //DB에 있는 리프레시 토큰 삭제 후 저장
        refreshTokenRepository.deleteByMemberId(memberDetail.getId());
        refreshTokenRepository.save(refreshToken);

        return LoginResponse.builder()
                .email(memberDetail.getUsername())
                .accessToken(tokenProvider.generateAccessToken(memberDetail.getId(), memberDetail.getUsername()))
                .refreshToken(refreshToken.getRefreshToken())
                .build();
    }

    public String reissueAccessToken(String refreshToken, HttpServletRequest request) {
        tokenProvider.validateToken(refreshToken);

        String expiredAccessToken = tokenProvider.resolveToken(request);

        RefreshToken originalToken = refreshTokenRepository.findByMemberId(tokenProvider.getUserId(expiredAccessToken))
                .orElseThrow(() -> new BaseException(BaseResponseStatus.FAIL_TOKEN_AUTHORIZATION));

        //액세스 토큰의 멤버와 리프레시 토큰의 멤버가 일치하는지 확인
        if(refreshToken.equals(originalToken.getRefreshToken())) {
            return tokenProvider.generateAccessToken(
                    tokenProvider.getUserId(expiredAccessToken),
                    tokenProvider.getEmail(expiredAccessToken));
        }
        throw new BaseException(BaseResponseStatus.FAIL_TOKEN_AUTHORIZATION);

    }


    public void deleteRefreshToken(MemberDetail memberDetail) {
        refreshTokenRepository.deleteByMemberId(memberDetail.getId());
    }

}

