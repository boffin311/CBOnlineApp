package com.codingblocks.onlineapi

import com.codingblocks.onlineapi.api.OnlineJsonApi
import com.codingblocks.onlineapi.api.OnlineRestApi
import com.codingblocks.onlineapi.api.OnlineVideosApi
import com.codingblocks.onlineapi.models.Announcement
import com.codingblocks.onlineapi.models.CarouselCards
import com.codingblocks.onlineapi.models.Certificate
import com.codingblocks.onlineapi.models.Choice
import com.codingblocks.onlineapi.models.Comment
import com.codingblocks.onlineapi.models.ContentCodeChallenge
import com.codingblocks.onlineapi.models.ContentCsv
import com.codingblocks.onlineapi.models.ContentDocumentType
import com.codingblocks.onlineapi.models.ContentLectureType
import com.codingblocks.onlineapi.models.ContentProgress
import com.codingblocks.onlineapi.models.ContentQna
import com.codingblocks.onlineapi.models.ContentVideoType
import com.codingblocks.onlineapi.models.Contents
import com.codingblocks.onlineapi.models.Course
import com.codingblocks.onlineapi.models.CourseSection
import com.codingblocks.onlineapi.models.DoubtsJsonApi
import com.codingblocks.onlineapi.models.Instructor
import com.codingblocks.onlineapi.models.InstructorSingle
import com.codingblocks.onlineapi.models.LectureContent
import com.codingblocks.onlineapi.models.MyCourse
import com.codingblocks.onlineapi.models.MyCourseRuns
import com.codingblocks.onlineapi.models.MyRunAttempt
import com.codingblocks.onlineapi.models.MyRunAttempts
import com.codingblocks.onlineapi.models.Note
import com.codingblocks.onlineapi.models.Notes
import com.codingblocks.onlineapi.models.Progress
import com.codingblocks.onlineapi.models.Question
import com.codingblocks.onlineapi.models.QuizAttempt
import com.codingblocks.onlineapi.models.Quizqnas
import com.codingblocks.onlineapi.models.Quizzes
import com.codingblocks.onlineapi.models.Rating
import com.codingblocks.onlineapi.models.RunAttemptsModel
import com.codingblocks.onlineapi.models.Sections
import com.codingblocks.onlineapi.models.Tags
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.jasminb.jsonapi.RelationshipResolver
import com.github.jasminb.jsonapi.ResourceConverter
import com.github.jasminb.jsonapi.retrofit.JSONAPIConverterFactory
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Clients {
    private val om = ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    var authJwt = ""
    private val onlineApiResourceConverter = ResourceConverter(
        om,
        Instructor::class.java,
        Course::class.java,
        Sections::class.java,
        Contents::class.java,
        MyCourseRuns::class.java,
        MyCourse::class.java,
        MyRunAttempts::class.java,
        MyRunAttempt::class.java,
        ContentVideoType::class.java,
        LectureContent::class.java,
        ContentDocumentType::class.java,
        ContentProgress::class.java,
        CourseSection::class.java,
        ContentLectureType::class.java,
        InstructorSingle::class.java,
        ContentCodeChallenge::class.java,
        ContentQna::class.java,
        Announcement::class.java,
        Progress::class.java,
        Quizzes::class.java,
        Question::class.java,
        Choice::class.java,
        QuizAttempt::class.java,
        RunAttemptsModel::class.java,
        Quizqnas::class.java,
        DoubtsJsonApi::class.java,
        ContentCsv::class.java,
        Comment::class.java,
        Note::class.java,
        Notes::class.java,
        Rating::class.java,
        Tags::class.java,
        Certificate::class.java,
        CarouselCards::class.java
    )
    private val relationshipResolver = RelationshipResolver {
        var url = it
        if (!it.contains("https")) {
            url = "https://api-online.cb.lk$url"
        }

        OkHttpClient()
            .newCall(Request.Builder().addHeader("Authorization", "JWT $authJwt").url(url).build())
            .execute()
            .body()
            ?.bytes()
    }

    //type resolver
    init {
        onlineApiResourceConverter.setGlobalResolver(relationshipResolver)
        onlineApiResourceConverter.enableDeserializationOption(com.github.jasminb.jsonapi.DeserializationFeature.ALLOW_UNKNOWN_INCLUSIONS)
        onlineApiResourceConverter.enableDeserializationOption(com.github.jasminb.jsonapi.DeserializationFeature.ALLOW_UNKNOWN_TYPE_IN_RELATIONSHIP)
    }

    private val ClientInterceptor = OkHttpClient.Builder()
        .addInterceptor { chain ->
            chain.proceed(chain.request().newBuilder().addHeader("Authorization", "JWT $authJwt").build())
        }
        .build()
    private val onlineV2JsonRetrofit = Retrofit.Builder()
        .client(ClientInterceptor)
        .baseUrl("https://api-online.cb.lk/api/v2/")
        .addConverterFactory(JSONAPIConverterFactory(onlineApiResourceConverter))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()
    val onlineV2JsonApi: OnlineJsonApi
        get() = onlineV2JsonRetrofit
            .create(OnlineJsonApi::class.java)
    private val retrofit = Retrofit.Builder()
        .client(ClientInterceptor)
        .baseUrl("https://api-online.cb.lk/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .build()
    val api: OnlineRestApi = retrofit.create(OnlineRestApi::class.java)
    var interceptor = CustomResponseInterceptor()
    private var client = OkHttpClient.Builder().addInterceptor(interceptor).build()
    //This client will download the video and m3u8 files from the server
    private val videoDownloadClient = Retrofit.Builder()
        .baseUrl("https://d1qf0ozss494xv.cloudfront.net/")
        .client(client)
        .build()
    private val apiVideo: OnlineVideosApi = videoDownloadClient.create(OnlineVideosApi::class.java)
    fun initiateDownload(url: String, fileName: String, keyPairId: String, signature: String, policy: String) = apiVideo.getVideoFiles(url, fileName, keyPairId, signature, policy)
}
