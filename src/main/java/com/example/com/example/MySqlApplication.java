package com.example.com.example;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;

import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.Action;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.PostbackEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.event.postback.PostbackContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ButtonsTemplate;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan
@LineMessageHandler
public class MySqlApplication {

	private int id = 1/*, mode = 0*/;
	private DataSource dataSource = null;
	//private String word = null, mean = null;

    @Autowired
    private JdbcTemplate jdbc;

    public static void main(String[] args) {
        SpringApplication.run(MySqlApplication.class, args);
    }



    @EventMapping
    public ReplyMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        System.out.println("event: " + event);//イベントをログに出力

        //String inputstr = event.getMessage().getText();//入力された文字列をinputstrに入れる
        String msg1;

        //switch (mode) {
        //case 0:
        		msg1 = "こんちわ	、なにかはかねにようじ？";
        		String thumbnailImageUrl = "https://riversun.github.io/img/riversun_256.png";
        		String title = "こうどうせんたく";
            Action addWords = new PostbackAction("コトバをオシえる", "addWords");
            Action readWords = new PostbackAction("コトバをキく", "readWords");
            Action readRecent = new PostbackAction("さいきんなにオボえた？", "readRecent");
            List<Action> actions = Arrays.asList(addWords, readWords, readRecent);
            ButtonsTemplate buttonsTemplate = new ButtonsTemplate(thumbnailImageUrl, title, msg1, actions);
        		String altText = title;
        		return new ReplyMessage(event.getReplyToken(), (Message) new TemplateMessage(altText, buttonsTemplate));
        //case 1:
        //		word = inputstr;
        //		mode = 2;
        //		msg1 = "それってどーゆーイミなの？";
        //		//obj = Arrays.asList(new TextMessage(msg1));
        //		return new ReplyMessage(event.getReplyToken(), Arrays.asList(new TextMessage(msg1)));
        //case 2:
        //		mean = inputstr;
        //		mode = 0;
        //		createColumn(word, mean);
        	//	msg1 = "オボえたよ。";
        		//obj = Arrays.asList(new TextMessage(msg1));
        	//	return new ReplyMessage(event.getReplyToken(), Arrays.asList(new TextMessage(msg1)));
        //case 3:
        //		mode = 0;
        //		List<Map<String, Object>> list2 = jdbc.queryForList
    		//		("SELECT means FROM instrument where words like '" + inputstr + "';");
        //		//テーブル「instrument」から文字列「入力値」を含む行から、カラム「comment」を取り出す。
        //		list2.forEach(System.out::println);   //取り出したカラムを文字列に変換。
        //		//以下で =以降の表示したい部分を決める。//
        //		//作りが悪いだけであって、うまく書けば不要な部分です。多分。//
        //		String string = list2.toString();
        //		int start = string.indexOf("=") + 1;
        //		int end = string.indexOf("}");
        //		String msg = string.substring(start,end);
        //		msg1 = "	『" + inputstr + "』は『" + msg + "』ってイミだよ。";
        //		//obj = Arrays.asList(new TextMessage(msg1));
        //		return new ReplyMessage(event.getReplyToken(), Arrays.asList(new TextMessage(msg1)));
        //}
    	}

    protected ReplyMessage handlePostbackEvent(PostbackEvent event) {
        // ButtonsTemplateでユーザーが選択した結果が、このPostBackEventとして返ってくる

        PostbackContent postbackContent = event.getPostbackContent();

        // PostbackActionで設定したdataを取得する
        String data = postbackContent.getData();
        String word = null, mean = null;
        String crlf = System.getProperty("line.separator");

        final String replyText;

        if ("addWords".equals(data)) {
            replyText = "なにをオシえてくれるの？";
            //mode = 1;
        } else if ("readWords".equals(data)) {
            replyText = "なにがシりたい？";
            //mode = 3;
        } else {
        		List<Map<String, Object>> list1 = jdbc.queryForList
        				("SELECT words FROM instrument where id like " + id + ";");
        		List<Map<String, Object>> list2 = jdbc.queryForList
        				("SELECT means FROM instrument where id like " + id + ";");
        		//テーブル「instrument」から文字列「入力値」を含む行から、カラム「comment」を取り出す。
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
            replyText = "さいきん『"+ word +"』をオボえたよ。" + crlf + "『" + mean + "』ってイミなんだって";
        }

        return new ReplyMessage(event.getReplyToken(), Arrays.asList(new TextMessage(replyText)));
    }

    public void createColumn(String word, String mean) {
        final String sql = "INSERT INTO instrument (words, means) VALUES ('" + word + "', '" + mean + "');";
        JdbcTemplate jt = new JdbcTemplate(this.dataSource);
        jt.execute(sql);
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("event: " + event);  //イベントをログに出力
    }
}