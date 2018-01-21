package com.example.com.example;

import java.util.List;
import java.util.Map;

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

    @Autowired
    private JdbcTemplate jdbc;

    public static void main(String[] args) {
        SpringApplication.run(MySqlApplication.class, args);
    }



    @EventMapping
    public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        System.out.println("event: " + event);//イベントをログに出力

        String inputstr = event.getMessage().getText();//入力された文字列をinputstrに入れる

      	List<Map<String, Object>> list2 = jdbc.queryForList
    			("SELECT means FROM instrument where words like '" + inputstr + "';");
    	 //テーブル「instrument」から文字列「入力値」を含む行から、カラム「comment」を取り出す。

        list2.forEach(System.out::println);   //取り出したカラムを文字列に変換。

    	//以下で =以降の表示したい部分を決める。//
        //作りが悪いだけであって、うまく書けば不要な部分です。多分。//
  	String string = list2.toString();
    	int start = string.indexOf("=") + 1;
    	int end = string.indexOf("}");
    	String new_str2 = string.substring(start,end);
    	new_str2 = "あ";
    	return new TextMessage(new_str2);  //new_str2を返す。
    	}

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("event: " + event);  //イベントをログに出力
    }
}