package ir.moke.microfox;

import ir.moke.kafir.http.Kafir;
import ir.moke.microfox.ftp.FtpClient;
import ir.moke.microfox.ftp.MicroFoxFtpConfig;
import ir.moke.microfox.http.Filter;
import ir.moke.microfox.http.Method;
import ir.moke.microfox.http.ResourceHolder;
import ir.moke.microfox.http.Route;
import ir.moke.microfox.job.JobSchedulerContainer;
import ir.moke.microfox.persistence.MicroFoxMyBatis;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.ibatis.session.SqlSession;
import org.quartz.Job;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.http.HttpClient;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class MicroFox {
    private static final Logger logger = LoggerFactory.getLogger(MicroFox.class);

    static {
        MicroFoxConfig.introduce();
    }

    public static void httpFilter(String path, Filter... filters) {
        ResourceHolder.instance.addFilter(path, filters);
    }

    public static void httpGet(String path, Route route) {
        ResourceHolder.instance.addRoute(Method.GET, path, route);
    }

    public static void httpPost(String path, Route route) {
        ResourceHolder.instance.addRoute(Method.POST, path, route);
    }

    public static void httpDelete(String path, Route route) {
        ResourceHolder.instance.addRoute(Method.DELETE, path, route);
    }

    public static void httpPut(String path, Route route) {
        ResourceHolder.instance.addRoute(Method.PUT, path, route);
    }

    public static void httpPatch(String path, Route route) {
        ResourceHolder.instance.addRoute(Method.PATCH, path, route);
    }

    public static void httpHead(String path, Route route) {
        ResourceHolder.instance.addRoute(Method.HEAD, path, route);
    }

    public static void httpOptions(String path, Route route) {
        ResourceHolder.instance.addRoute(Method.OPTIONS, path, route);
    }

    public static void httpTrace(String path, Route route) {
        ResourceHolder.instance.addRoute(Method.TRACE, path, route);
    }

    public static <T> void httpCall(String baseUri, Class<T> serviceClass, Consumer<T> consumer) {
        httpCall(baseUri, Map.of(), serviceClass, consumer);
    }

    public static <T> void httpCall(String baseUri, Map<String, String> headers, Class<T> serviceClass, Consumer<T> consumer) {
        T t = new Kafir.KafirBuilder()
                .setBaseUri(baseUri)
                .setVersion(HttpClient.Version.HTTP_2)
                .setHeaders(headers)
                .build(serviceClass);
        consumer.accept(t);
    }

    public static void job(Class<? extends Job> jobClass, String cronExpression) {
        JobSchedulerContainer.instance.register(jobClass, cronExpression);
    }

    public static void job(Class<? extends Job> jobClass, ZonedDateTime zonedDateTime) {
        JobSchedulerContainer.instance.register(jobClass, Date.from(zonedDateTime.toInstant()));
    }

    public static <T, R> R sqlFetch(String databaseId, Class<T> mapper, Function<T, R> function) {
        try (SqlSession sqlSession = MicroFoxMyBatis.getSqlSessionFactory(databaseId).openSession(true)) {
            T t = sqlSession.getMapper(mapper);
            return function.apply(t);
        }
    }

    public static <T> void sqlExecute(String databaseId, Class<T> mapper, Consumer<T> consumer) {
        try (SqlSession sqlSession = MicroFoxMyBatis.getSqlSessionFactory(databaseId).openSession(true)) {
            T t = sqlSession.getMapper(mapper);
            consumer.accept(t);
        }
    }

    public static <T> void sqlBatch(String databaseId, Class<T> mapper, Consumer<T> consumer) {
        SqlSession batchSession = MicroFoxMyBatis.getBatchSession(databaseId);
        T t = batchSession.getMapper(mapper);
        try {
            consumer.accept(t);
            batchSession.commit();
        } catch (Exception e) {
            batchSession.rollback();
            throw new RuntimeException("Failed to execute SQL batch", e);
        } finally {
            batchSession.close();
        }
    }

    public static void ftpDownload(MicroFoxFtpConfig microFoxFtpConfig, String remoteFilePath, Path localDownloadDir) {
        try {
            FtpClient ftpClient = new FtpClient();
            ftpClient.connect(microFoxFtpConfig.host(), microFoxFtpConfig.port());
            ftpClient.login(microFoxFtpConfig.username(), microFoxFtpConfig.password());
            ftpClient.ftpDownload(remoteFilePath, localDownloadDir);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void ftpBatchDownload(MicroFoxFtpConfig microFoxFtpConfig, List<String> remoteFilePath, Path localDownloadDir) {
        try {
            FtpClient ftpClient = new FtpClient();
            ftpClient.connect(microFoxFtpConfig.host(), microFoxFtpConfig.port());
            ftpClient.login(microFoxFtpConfig.username(), microFoxFtpConfig.password());
            ftpClient.ftpBatchDownload(remoteFilePath, localDownloadDir);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void ftpUpload(MicroFoxFtpConfig microFoxFtpConfig, String remoteFilePath, File file) {
        try {
            FtpClient ftpClient = new FtpClient();
            ftpClient.connect(microFoxFtpConfig.host(), microFoxFtpConfig.port());
            ftpClient.login(microFoxFtpConfig.username(), microFoxFtpConfig.password());
            ftpClient.ftpUpload(remoteFilePath, file);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static void ftpBatchUpload(MicroFoxFtpConfig microFoxFtpConfig, String remoteFilePath, List<File> files) {
        try {
            FtpClient ftpClient = new FtpClient();
            ftpClient.connect(microFoxFtpConfig.host(), microFoxFtpConfig.port());
            ftpClient.login(microFoxFtpConfig.username(), microFoxFtpConfig.password());
            ftpClient.ftpBatchUpload(remoteFilePath, files);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static void ftpDelete(MicroFoxFtpConfig microFoxFtpConfig, String remoteFilePath) {
        try {
            FtpClient ftpClient = new FtpClient();
            ftpClient.connect(microFoxFtpConfig.host(), microFoxFtpConfig.port());
            ftpClient.login(microFoxFtpConfig.username(), microFoxFtpConfig.password());
            ftpClient.delete(remoteFilePath);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void ftpList(MicroFoxFtpConfig microFoxFtpConfig, String remoteFilePath, Consumer<FTPFile[]> consumer) {
        try {
            FtpClient ftpClient = new FtpClient();
            ftpClient.connect(microFoxFtpConfig.host(), microFoxFtpConfig.port());
            ftpClient.login(microFoxFtpConfig.username(), microFoxFtpConfig.password());
            consumer.accept(ftpClient.listFiles(remoteFilePath));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
