package jp.techacademy.kikuchi.toru.qa_app;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.prefs.Preferences;
/*
ログインしている場合に質問詳細画面に「お気に入り」ボタンを表示させてください
　ToDo:☆をDetailに追加 -> Firebaes側に設定値を設ける
ログインしている場合に「お気に入り」が既にタップされているかどうかをボタンの見た目で判断できるようにしてください
　ToDo:★を一覧に追加
「お気に入り」質問の一覧画面を作成してください
　ToDo:「お気に入り」を追加 -> Done
ログインしている場合にドロワーメニューに「お気に入り」一覧画面へ遷移するリンクを追加してください
　ToDo:左から出てくるところに「お気に入り」を追加
 */

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private int mGenre = 0;

    // --- ここから ---
    private DatabaseReference mDatabaseReference;
    private DatabaseReference mGenreRef;
    private DatabaseReference mFavRef;
    private ListView mListView;
    private ArrayList<Question> mQuestionArrayList;
    private QuestionsListAdapter mAdapter;
    private DatabaseReference mFavReference;

    private ChildEventListener mFavoriteListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            HashMap map = (HashMap) dataSnapshot.getValue();
            String title = (String) map.get("title");
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");
            String favorite = (String) map.get("favorite");
            String imageString = (String) map.get("image");

            String favGenre = dataSnapshot.getRef().getParent().toString();
            int g = Integer.parseInt(""+favGenre.charAt(favGenre.length()-1));
            //Log.v("QA",String.valueOf(g));

            byte[] bytes;
            if (imageString != null) {
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap) map.get("answers");
            if (answerMap != null) {
                for (Object key : answerMap.keySet()) {
                    HashMap temp = (HashMap) answerMap.get((String) key);
                    String answerBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                    answerArrayList.add(answer);
                }
            }

            if(uid.equals(user.getUid()) == true) {
                if (favorite.equals("1") == true) {
                    Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), favorite, g, bytes, answerArrayList);
                    mQuestionArrayList.add(question);
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // 変更があったQuestionを探す
            for (Question question: mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ChildEventListener mEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            //Log.v("QA",user.getUid());

            HashMap map = (HashMap) dataSnapshot.getValue();
            String title = (String) map.get("title");
            String body = (String) map.get("body");
            String name = (String) map.get("name");
            String uid = (String) map.get("uid");
            String favorite = (String) map.get("favorite");
            String imageString = (String) map.get("image");
            byte[] bytes;
            if (imageString != null) {
                bytes = Base64.decode(imageString, Base64.DEFAULT);
            } else {
                bytes = new byte[0];
            }

            ArrayList<Answer> answerArrayList = new ArrayList<Answer>();
            HashMap answerMap = (HashMap) map.get("answers");
            if (answerMap != null) {
                for (Object key : answerMap.keySet()) {
                    HashMap temp = (HashMap) answerMap.get((String) key);
                    String answerBody = (String) temp.get("body");
                    String answerName = (String) temp.get("name");
                    String answerUid = (String) temp.get("uid");
                    Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                    answerArrayList.add(answer);
                }
            }
            if(uid.equals(user.getUid()) == true) {
                Question question = new Question(title, body, name, uid, dataSnapshot.getKey(), favorite, mGenre, bytes, answerArrayList);
                mQuestionArrayList.add(question);
                mAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            HashMap map = (HashMap) dataSnapshot.getValue();

            // 変更があったQuestionを探す
            for (Question question: mQuestionArrayList) {
                if (dataSnapshot.getKey().equals(question.getQuestionUid())) {
                    // このアプリで変更がある可能性があるのは回答(Answer)のみ
                    question.getAnswers().clear();
                    HashMap answerMap = (HashMap) map.get("answers");
                    if (answerMap != null) {
                        for (Object key : answerMap.keySet()) {
                            HashMap temp = (HashMap) answerMap.get((String) key);
                            String answerBody = (String) temp.get("body");
                            String answerName = (String) temp.get("name");
                            String answerUid = (String) temp.get("uid");
                            Answer answer = new Answer(answerBody, answerName, answerUid, (String) key);
                            question.getAnswers().add(answer);
                        }
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };
    // --- ここまで追加する ---




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        //Preferencesからお気に入りを取得する
        //mPrefs = Gdx.app.getPreferences("jp.techacademy.taro.kirameki.jumpactiongame");
        //mHighScore = mPrefs.getInteger("HIGHSCORE", 0);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
                if (mGenre == 0) {
                    Snackbar.make(view, "ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show();
                    return;
                }
                // ジャンルを選択していない場合（mGenre == 0）はエラーを表示するだけ
                if (mGenre == -1) {
                    Snackbar.make(view, "お気に入りでは追加できません。ジャンルを選択して下さい", Snackbar.LENGTH_LONG).show();
                    return;
                }

                // ログイン済みのユーザーを収録する
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user == null) {
                    // ログインしていなければログイン画面に遷移させる
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                } else {
                    // ジャンルを渡して質問作成画面を起動する
                    Intent intent = new Intent(getApplicationContext(), QuestionSendActivity.class);
                    intent.putExtra("genre", mGenre);
                    startActivity(intent);
                }

            }
        });

        // ナビゲーションドロワーの設定
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, mToolbar, R.string.app_name, R.string.app_name);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_hobby) {
                    mToolbar.setTitle("趣味");
                    mGenre = 1;
                } else if (id == R.id.nav_life) {
                    mToolbar.setTitle("生活");
                    mGenre = 2;
                } else if (id == R.id.nav_health) {
                    mToolbar.setTitle("健康");
                    mGenre = 3;
                } else if (id == R.id.nav_compter) {
                    mToolbar.setTitle("コンピューター");
                    mGenre = 4;
                } else if (id == R.id.nav_favorite) {
                    mToolbar.setTitle("お気に入り");
                    //お気に入りはジャンルではないため
                    mGenre = -1;
                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                mQuestionArrayList.clear();

                // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                //mQuestionArrayList.clear();
                mAdapter.setQuestionArrayList(mQuestionArrayList);
                mListView.setAdapter(mAdapter);
                if (mGenreRef != null) {
                    mGenreRef.removeEventListener(mEventListener);
                }
                if (mFavRef != null) {
                    mFavRef.removeEventListener(mFavoriteListener);
                    //mFavReference.removeEventListener(mFavoriteListener);
                }
                //お気に入り一覧の取得
                if(mGenre==-1){

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    if(user == null){
                        Snackbar.make(mListView,"ログインを先にしてください",Snackbar.LENGTH_SHORT).show();
                    }

                    //Log.v("QA",String.valueOf(mQuestionArrayList.size()));
                    // 選択したジャンルにリスナーを登録する
                    //mFavReference = mDatabaseReference.child(Const.ContentsPATH);
                    //Log.v("QA",mFavReference.toString());
                    mFavRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(1));
                    mFavRef.addChildEventListener(mFavoriteListener);
                    mFavRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(2));
                    mFavRef.addChildEventListener(mFavoriteListener);
                    mFavRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(3));
                    mFavRef.addChildEventListener(mFavoriteListener);
                    mFavRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(4));
                    mFavRef.addChildEventListener(mFavoriteListener);

                }else {

                    mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
                    mGenreRef.addChildEventListener(mEventListener);

                }
                return true;
            }
        });
        // Firebase
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        // ListViewの準備
        mListView = (ListView) findViewById(R.id.listView);
        mAdapter = new QuestionsListAdapter(MainActivity.this);
        mQuestionArrayList = new ArrayList<Question>();
        mAdapter.notifyDataSetChanged();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Questionのインスタンスを渡して質問詳細画面を起動する
                Intent intent = new Intent(getApplicationContext(), QuestionDetailActivity.class);
                intent.putExtra("question", mQuestionArrayList.get(position));
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume(){
        super.onResume();
        // ログイン済みのユーザーを収録する
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            // ログインしていなければログイン画面に遷移させる
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
        } else {
            // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
            mQuestionArrayList.clear();
            //mQuestionArrayList.clear();
            mAdapter.setQuestionArrayList(mQuestionArrayList);
            mListView.setAdapter(mAdapter);
            if (mGenreRef != null) {
                mGenreRef.removeEventListener(mEventListener);
            }
            if (mFavRef != null) {
                mFavRef.removeEventListener(mFavoriteListener);
            }
            //お気に入り一覧の取得
            if (mGenre == -1) {
                //Snackbar.make(mListView, "ログインを先にしてください", Snackbar.LENGTH_SHORT).show();

                //Log.v("QA",String.valueOf(mQuestionArrayList.size()));
                // 質問のリストをクリアしてから再度Adapterにセットし、AdapterをListViewにセットし直す
                // 選択したジャンルにリスナーを登録する
                mFavRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(1));
                mFavRef.addChildEventListener(mFavoriteListener);
                mFavRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(2));
                mFavRef.addChildEventListener(mFavoriteListener);
                mFavRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(3));
                mFavRef.addChildEventListener(mFavoriteListener);
                mFavRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(4));
                mFavRef.addChildEventListener(mFavoriteListener);

            } else {

                mGenreRef = mDatabaseReference.child(Const.ContentsPATH).child(String.valueOf(mGenre));
                mGenreRef.addChildEventListener(mEventListener);

            }
        }
     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), SettingActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}