package org.xyz.luckyjourney.util;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.netty.util.internal.StringUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class JwtUtil {

    public static final long EXPIRE = 10000000 * 60 * 60 * 24;

    public static final String APP_SECRET = "ukc8BDbRigUDaY6pZFfWus2jZWLPHO";


    public static String getJwtToken(Long id,String nickName){

        return Jwts.builder()
                .setHeaderParam("typ","jwt")
                .setHeaderParam("alg","H256")

                .setSubject("guli-user")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE))

                .claim("id",String.valueOf(id))
                .claim("nickname",nickName)

                .signWith(SignatureAlgorithm.HS256,APP_SECRET)
                .compact();
    }

    public static Boolean checkToken(String jwtToken){
        if(StringUtils.isEmpty(jwtToken)){
            return false;
        }

        try {
            Jwts.parser().setSigningKey(APP_SECRET).parseClaimsJws(jwtToken);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static Boolean checkToken(HttpServletRequest request){

        try {
            String token = request.getHeader("token");

            if(StringUtils.isEmpty(token)){
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static Long getUserId(HttpServletRequest request){
        String token = request.getHeader("token");
        if(ObjectUtils.isEmpty(token)){
            return null;
        }
        Jws<Claims> claimsJws = Jwts.parser().setSigningKey(APP_SECRET).parseClaimsJws(token);
        Claims claims = claimsJws.getBody();
        return Long.valueOf(claims.get("id").toString());
    }
}
