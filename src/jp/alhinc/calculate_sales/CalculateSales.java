package jp.alhinc.calculate_sales;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalculateSales {

	// 支店定義ファイル名
	private static final String FILE_NAME_BRANCH_LST = "branch.lst";

	// 支店別集計ファイル名
	private static final String FILE_NAME_BRANCH_OUT = "branch.out";

	// エラーメッセージ
	private static final String UNKNOWN_ERROR = "予期せぬエラーが発生しました";
	private static final String FILE_NOT_EXIST = "支店定義ファイルが存在しません";
	private static final String FILE_INVALID_FORMAT = "支店定義ファイルのフォーマットが不正です";

	/**
	 * メインメソッド
	 *
	 * @param コマンドライン引数
	 */
	public static void main(String[] args) {
		// 支店コードと支店名を保持するMap
		Map<String, String> branchNames = new HashMap<>();
		// 支店コードと売上金額を保持するMap
		Map<String, Long> branchSales = new HashMap<>();

		// 支店定義ファイル読み込み処理
		if (!readFile(args[0], FILE_NAME_BRANCH_LST, branchNames, branchSales)) {
			return;
		}

		// ※ここから集計処理を作成してください。(処理内容2-1、2-2)
		//C:\Users\trainee1441\Desktop\売上集計課題　フォルダからすべてのファイルを取得する
		File[] files = new File(args[0]).listFiles();

		//先にファイルの情報を格納する List(ArrayList) を宣⾔します。
		List<File> rcdFiles = new ArrayList<>();

		for (int i = 0; i < files.length; i++) {

			//rcdかどうか確認する
			//8桁かどうかを確認する
			//一致したら(trueの場合)リストに入れる、一致しなかったら(False)何もしない（51行目に進む、if文の中に入らない）
			if (files[i].getName().matches("^\\d{8}.rcd$")) {
				//リストに入れる。売上ファイルの条件に当てはまったものだけ、List(ArrayList) に追加します。
				rcdFiles.add(files[i]);
			}//↑ここまででrcdFilesに売上ファイル(8桁のrcdファイル)を追加した
		}

		 //ここで売上ファイルが連番になっているか確認する
		//⽐較回数は売上ファイルの数よりも1回少ないため、
		//繰り返し回数は売上ファイルのリストの数よりも1つ⼩さい数です。
		//i-1回繰り返します
		for(int i = 0; i < rcdFiles.size() -1; i++) {

			int former = Integer.parseInt(rcdFiles.get(i).getName().substring(0, 8));
			int latter = Integer.parseInt(rcdFiles.get(i+1).getName().substring(0, 8));
		      //↑⽐較する2つのファイル名の先頭から数字の8⽂字を切り出し、int型に変換しました。

			if((latter - former) != 1) {
				//2つのファイル名の数字を⽐較して、差が1ではなかったら、
				//エラーメッセージをコンソールに表⽰します。
				System.out.println("売上ファイル名が連番になっていません");
			}
		}


		//rcdFilesに複数の売上ファイルの情報を格納しているので、その数だけ繰り返します。
		for (int i = 0; i < rcdFiles.size(); i++) {

			//支店定義ファイル読み込み(readFileメソッド)を参考に売上ファイルの中身を読み込みます。
			//売上ファイルの1行目には支店コード、2行目には売上金額が入っています。
			BufferedReader br = null;

			try {
				//ファイルを開く
				File file = new File(args[0], rcdFiles.get(i).getName());
				FileReader fr = new FileReader(file);
				br = new BufferedReader(fr);

				//lineの使い道…読み込んだ直後の一旦の受け皿
				String line;

				//fileContentsの使い道…この後の足し算等に使用
				List<String> fileContents = new ArrayList<>();

				// readlineメソッドを使って一行読んだら、lineに入れる(代入)
				while ((line = br.readLine()) != null) {
					//lineに入っている文字列を、リストに格納（add）
					fileContents.add(line);
				}

				//売上ファイルから読み込んだ売上金額をMapに加算していくために、型の変換を行います。
				//※詳細は後述で説明
				long fileSale = Long.parseLong(fileContents.get(1));

				//読み込んだ売上⾦額を加算します。
				// branchSales.get…マップから、値(value)を取り出せる！
				//saleAmount… branchSales（1回目は0円）と fileSale（売上）を足したもの。
				Long saleAmount = branchSales.get(fileContents.get(0)) + fileSale;

				if(saleAmount >= 10000000000L){
					//売上⾦額が11桁以上の場合、エラーメッセージをコンソールに表⽰します。
					//System.out.println()

				//加算した売上⾦額をMapに追加します。
				branchSales.put(fileContents.get(0), saleAmount);




				}
			} catch (IOException e) {
				System.out.println(UNKNOWN_ERROR);
				return;




			} finally {
				// ファイルを開いている場合
				if (br != null) {
					try {
						// ファイルを閉じる
						br.close();
					} catch (IOException e) {
						System.out.println(UNKNOWN_ERROR);
						return;
					}
				}
			}

		}

		// 支店別集計ファイル書き込み処理
		if (!writeFile(args[0], FILE_NAME_BRANCH_OUT, branchNames, branchSales)) {
			return;
		}

	}

	/**
	 * 支店定義ファイル読み込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 読み込み可否
	 */
	private static boolean readFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		//変数の宣言
		BufferedReader br = null;

		try {
			//ファイルを開く
			File file = new File(path, fileName);
			if(!file.exists()) {
				 System.out.println(FILE_NOT_EXIST);
				 return false;
			    //⽀店定義ファイルが存在しない場合、コンソールにエラーメッセージを表⽰します。
			}
			FileReader fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line;
			// readlineメソッドを使って一行読んだら、lineに入れる(代入)
			while ((line = br.readLine()) != null) {
				// ※ここの読み込み処理を変更してください。(処理内容1-2)
				//読んだもの（line）をスプリッドして（カンマ,を境に）、itemsに入れる
				String[] items = line.split(",");

				//配列（items）に２つ、または３桁の数字かどうかを判断
				if(items.length != 2 || !items[0].matches("^\\d{3}$")){
				    //⽀店定義ファイルの仕様が満たされていない場合、
				    //エラーメッセージをコンソールに表⽰します。{
						 System.out.println(UNKNOWN_ERROR);
						 return false;
				}

				//branchNamesにitemsを追加（put）している
				branchNames.put(items[0], items[1]);
				branchSales.put(items[0], 0L);

			}


		} catch (IOException e) {
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (br != null) {
				try {
					// ファイルを閉じる
					br.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 支店別集計ファイル書き込み処理
	 *
	 * @param フォルダパス
	 * @param ファイル名
	 * @param 支店コードと支店名を保持するMap
	 * @param 支店コードと売上金額を保持するMap
	 * @return 書き込み可否
	 */
	private static boolean writeFile(String path, String fileName, Map<String, String> branchNames,
			Map<String, Long> branchSales) {
		// ※ここに書き込み処理を作成してください。(処理内容3-1)

		//変数の宣言
		BufferedWriter bw = null;

		try {
			//ファイルを作成し、書き込む処理
			//ファイルを作るpath(場所)、fileName(名前)を指定
			File file = new File(path, fileName);
			//実際にファイルを作る
			FileWriter fw = new FileWriter(file);
			//作ったファイルに今から書き込むよ！という命令をする
			bw = new BufferedWriter(fw);

			for (String key : branchNames.keySet()) {
				//keyという変数には、Mapから取得したキー（＝支店コード）が代入されています。
				//拡張for⽂で繰り返されているので、1つ⽬のキーが取得できたら、
				//2つ⽬の取得...といったように、次々とkeyという変数に上書きされていきます。

				//作ったファイルに文字列(支店コード key +支店名 branchNamesのvalue+ 売上 branchSalesのvalue)を書き込む
				bw.write(key + "," + branchNames.get(key) + "," + branchSales.get(key));
				//改⾏する
				bw.newLine();

			}

		} catch (IOException e) {
			//エラーメッセージの表示
			System.out.println(UNKNOWN_ERROR);
			return false;
		} finally {
			// ファイルを開いている場合
			if (bw != null) {
				try {
					// ファイルを閉じる
					bw.close();
				} catch (IOException e) {
					System.out.println(UNKNOWN_ERROR);
					return false;
				}
			}
		}
		return true;

	}

}
