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

	private int mode = 0;                                      //モード切り替え用変数
	private String word = null, mean = null;                   //カラムに入れるコトバとイミを格納する変数
	final String crlf = System.getProperty("line.separator");  //環境別で改行のコードを獲得
	private boolean flag = false;                              //入力待ちフラグ


    @Autowired
    private JdbcTemplate jdbc;                                 //jdbcドライバのオブジェクト
	private DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(MySqlApplication.class, args);
    }



    @EventMapping
    public TextMessage handleTextMessageEvent(MessageEvent<TextMessageContent> event) throws SQLException {
        System.out.println("event: " + event); //イベントをログに出力

        //String userID = event.getSource().getUserId(); //ユーザーIDを取得してuserIDに入れる
        String inputstr = event.getMessage().getText(); //入力された文字列をinputstrに入れる

        String msg1 = null; //リプライ用変数

        if (flag == true) { //リプライによるモード分岐
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

        case 0: //モード選択
        		msg1 = "なにかはかねにようじ？" + crlf +
        				"コトバをおしえる　　　:あ" + crlf +
        				"コトバをしりたい　　　:い" + crlf +
        				"さいきんなにオボえた？:う";
        		flag = true;
        		break;

        case 1: //イミ入力待ち(モード"あ")
        		word = inputstr;
        		mode = 2;
        		msg1 = "それってどーゆーイミなの？";
        		break;

        case 2: //オボえた(モード"あ"完了リプライ)
        		mean = inputstr;
        		mode = 0;
        		createColumn(word, mean); //word
        		msg1 = "オボえたよ。";
        		break;

        case 3: //コトバをリプライする(モード"い")
        		mode = 0; //モード切り替え

        		List<Map<String, Object>> list = jdbc.queryForList
    				("SELECT means FROM instrument where words like '" + inputstr + "';");
        		//テーブル「instrument」から文字列「入力値」を含む行から、カラム「means」を取り出す。
        		list.forEach(System.out::println);   //取り出したカラムを文字列に変換。
        		//以下で =以降の表示したい部分を決める。//
        		String string = list.toString();
        		int start = string.indexOf("=") + 1;
        		int end = string.indexOf("}");
        		String msg = string.substring(start,end);

        		//データベース上にデータがあればmsg1にリプライを格納して終了、なければモード"あ"に切り替え
        		if (msg != null) msg1 = "『" + inputstr + "』は『" + msg + "』ってイミだよ。";
        		else {
        			msg1 = "そのコトバはシらないよ、オシえて？";
        			mode = 2;
        		}
        		break;

        case 4: //コトバ入力待ち(モード"あ")
        		msg1 = "なにをオシえてくれるの？";
        		flag = false; //フラグを下ろす
        		mode = 1; //イミ入力待ちへ
        		break;

        case 5: //コトバ入力待ち(モード"い")
        		msg1 = "なにがシりたい？";
        		flag = false; //フラグを下ろす
        		mode = 3; //イミ入力待ちへ
        		break;

        case 6: //モード"う"実行
        		int id = jdbc.queryForObject("SELECT MAX(id) FROM instrument;", Integer.class);
        		//idが最大の行からidの値を取得する
        		List<Map<String, Object>> list1 = jdbc.queryForList
        				("SELECT words FROM instrument where id like " + id + ";");
        		List<Map<String, Object>> list2 = jdbc.queryForList
        				("SELECT means FROM instrument where id like " + id + ";");
        		//テーブル「instrument」から数値「id」を含む行から、カラム「words」と「means」を取り出す。

        		list1.forEach(System.out::println);   //取り出したカラムを文字列に変換。
        		list2.forEach(System.out::println);   //取り出したカラムを文字列に変換。

        		//以下で =以降の表示したい部分を決める。//
        		String string1 = list1.toString();
        		int start1 = string1.indexOf("=") + 1;
        		int end1 = string1.indexOf("}");
        		word = string1.substring(start1,end1);
        		String string2 = list2.toString();
        		int start2 = string2.indexOf("=") + 1;
        		int end2 = string2.indexOf("}");
        		mean = string2.substring(start2,end2);

        		msg1 = "さいきん『"+ word +"』をオボえたよ。" + crlf + "『" + mean + "』ってイミなんだって";

        		flag = false; //フラグを下ろす
        		mode = 0; //モード切り替え
        		break;
        }
        	return new TextMessage(msg1); //TextMessage型の変数を戻り値として返す
    	}

    public void createColumn(String word, String mean) { //カラムをCREATEする関数
        final String sql = "INSERT INTO instrument (words, means) VALUES ('" + word + "', '" + mean + "');";
        jdbc.execute(sql);
    }

    public String getFirstNameById(int id) { //今後の拡張用
        final String sql = "select first_name from person where id=?";
        JdbcTemplate jt = new JdbcTemplate(this.dataSource);
        return (String) jt.queryForObject(sql,
                                          new Object[]{new Integer(id)},
                                          String.class);
    }

    public void setDataSource(DataSource dataSource) { //今後の拡張用
        this.dataSource = dataSource;
    }

    @EventMapping
    public void handleDefaultMessageEvent(Event event) {
        System.out.println("event: " + event);  //イベントをログに出力
    }
}