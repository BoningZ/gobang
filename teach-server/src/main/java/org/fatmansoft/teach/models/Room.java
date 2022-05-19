package org.fatmansoft.teach.models;



import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Date;

@Proxy(lazy = false)
@Entity
@Table(	name = "room",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "rid"),
        })
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rid;

    @ManyToOne
    @JoinColumn(name = "hostId")
    private Student host;

    @ManyToOne
    @JoinColumn(name = "guestId")
    private Student guest;

    private Date startDate;

    @Size(max = 255)
    private String chess=new String(new char[255]).replace("\0","0");//0null 1black 2white


    public void setChess(int i,int j,int c){
        StringBuilder sb=new StringBuilder(chess);
        sb.setCharAt((i-1)*15+j,(char)(c+'0'));
        chess=new String(sb);
    }

    public void setChess(int i,int j){
        if(isBlack())setChess(i,j,1);
        else setChess(i,j,2);
    }

    public boolean isBlack(){
        int[] basket=new int[3];
        for(int i=0;i<chess.length();i++)basket[chess.charAt(i)-'0']++;
        return basket[1]<=basket[2];
    }


    public int wins(){
        char[][] brd= new char[16][16];
        int s=0;
        for(int i=1;i<=15;i++)for(int j=1;j<=15;j++)brd[i][j]=chess.charAt(s++);
        for(int i=1;i<=15;i++)for(int j=1;j<=15;j++){
            if(brd[i][j]=='0')continue;
            char c=brd[i][j];
            if(j<=11)for(int k=1;brd[i][j+k]==c;k++)if(k==4)return c-'0';
            if(i<=11)for(int k=1;brd[i+k][j]==c;k++)if(k==4)return c-'0';
            if(j<=11&&i<=11)for(int k=1;brd[i+k][j+k]==c;k++)if(k==4)return c-'0';
            if(j<=11&&i>=5 )for(int k=1;brd[i-k][j+k]==c;k++)if(k==4)return c-'0';
        }
        return 0;
    }

    public Integer getRid() {
        return rid;
    }

    public void setRid(Integer rid) {
        this.rid = rid;
    }

    public Student getHost() {
        return host;
    }

    public void setHost(Student host) {
        this.host = host;
    }

    public Student getGuest() {
        return guest;
    }

    public void setGuest(Student guest) {
        this.guest = guest;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getChess() {
        return chess;
    }

    public void setChess(String chess) {
        this.chess = chess;
    }
}

