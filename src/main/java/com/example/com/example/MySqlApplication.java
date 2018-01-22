package com.example.com.example;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;

import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
@LineMessageHandler
public class MySqlApplication {

	private DataSource dataSource = null;
	private int mode = 0;
	private String word = null, mean = null;
	final String crlf = System.getProperty("line.separator");
	private boolean flag = false;

    @Autowired
    private JdbcTemplate jdbc;

    public static void main(String[] args) {
        SpringApplication.run(MySqlApplication.class, args);
    }



    @EventMapping
    public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws SQLException {
        System.out.println("event: " + event);//イベントをログに出力

        String inputstr = event.getMessage().getText();//入力された文字列をinputstrに入れる
        String msg1 = null;
        if (flag == true) {
        		switch (inputstr) {
        		case "あ":
        			mode = 4;
        			break;
        		case "い":
        			mode = 5;
        			break;
        		case "う":
        			mode = 6;
        			break;
        		}
        } else {
        }

        switch (mode) {
        case 0:
        		msg1 = "なにかはかねにようじ？" + crlf +
        				"コトバをおしえる　　　:あ" + crlf +
        				"コトバをしりたい　　　:い" + crlf +
        				"さいきんなにオボえた？:う";
        		flag = true;
        		break;
        case 1:
        		word = inputstr;
        		mode = 2;
        		msg1 = "それってどーゆーイミなの？";
        		break;
        case 2:
        		mean = inputstr;
        		mode = 0;
        		createColumn(word, mean);
        		msg1 = "オボえたよ。";
        		break;
        case 3:
        		mode = 0;
        		List<Map<String, Object>> list = jdbc.queryForList
    				("SELECT means FROM instrument where words like '" + inputstr + "';");
        		//テーブル「instrument」から文字列「入力値」を含む行から、カラム「comment」を取り出す。
        		list.forEach(System.out::println);   //取り出したカラムを文字列に変換。
        		//以下で =以降の表示したい部分を決める。//
        		//作りが悪いだけであって、うまく書けば不要な部分です。多分。//
        		String string = list.toString();
        		int start = string.indexOf("=") + 1;
        		int end = string.indexOf("}");
        		String msg = string.substring(start,end);
        		msg1 = "	『" + inputstr + "』は『" + msg + "』ってイミだよ。";
        		break;
        case 4:
        		msg1 = "なにをオシえてくれるの？";
        		flag = false;
        		mode = 1;
        		break;
        case 5:
        		msg1 = "なにがシりたい？";
        		flag = false;
        		mode = 3;
        		break;
        case 6:
        	int id = jdbc.queryForObject("SELECT MAX(id) FROM instrument;", Integer.class);
        	List<Map<String, Object>> list1 = jdbc.queryForList
			("SELECT words FROM instrument where id like " + id + ";");
        	List<Map<String, Object>> list2 = jdbc.queryForList
			("SELECT means FROM instrument where id like " + id + ";");
        	//テーブル「instrument」から文字列「入力値」を含む行から、カラム「comment」を取り出す。
        	list1.forEach(System.out::println);   //取り出したカラムを文字列に変換。
        	list2.forEach(System.out::println);   //取り出したカラムを文字列に変換。

        	//以下で =以降の表示したい部分を決める。//
        	//作りが悪いだけであって、うまく書けば不要な部分です。多分。//
        	String string1 = list1.toString();
        	int start1 = string1.indexOf("=") + 1;
        	int end1 = string1.indexOf("}");
        	word = string1.substring(start1,end1);
        	String string2 = list2.toString();
        	int start2 = string2.indexOf("=") + 1;
        	int end2 = string2.indexOf("}");
        	mean = string2.substring(start2,end2);
        	msg1 = "さいきん『"+ word +"』をオボえたよ。" + crlf + "『" + mean + "』ってイミなんだって";
        	flag = false;
        mode = 0;
        	break;
        }
        	return new TextMessage(msg1);
    	}

    public void createColumn(String word, String mean) {
        final String sql = "INSERT INTO instrument (words, means) VALUES ('" + word + "', '" + mean + "');";
        jdbc.execute(sql);
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("event: " + event);  //イベントをログに出力
    }
}