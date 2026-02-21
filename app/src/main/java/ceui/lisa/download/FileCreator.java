package ceui.lisa.download;

import android.text.TextUtils;

import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ceui.lisa.activities.Shaft;
import ceui.lisa.file.FileName;
import ceui.lisa.helper.FileStorageHelper;
import ceui.lisa.model.CustomFileNameCell;
import ceui.lisa.models.IllustsBean;
import ceui.lisa.models.NovelBean;
import ceui.lisa.models.NovelSeriesItem;
import ceui.lisa.utils.Common;

public class FileCreator {

    private static final String DASH = "_";

    public static boolean isExist(IllustsBean illust, int index) {
        String fileName = illust.isGif() ? new FileName().gifName(illust) : customFileName(illust, index);
        File file = new File(FileStorageHelper.getIllustAbsolutePath(illust), fileName);
        Common.showLog("saasdadw 给是否存在 " + file.getPath());
        return file.exists();
    }

    public static String deleteSpecialWords(String before) {
        if (!TextUtils.isEmpty(before)) {
            if(before.startsWith(".")){
                before = before.replaceFirst("\\.","\u2024");
            }
            String temp1 = before.replace("-", DASH);
            String temp2 = temp1.replace("/", DASH);
            String temp3 = temp2.replace(",", DASH);
            String temp4 = temp3.replace(":", DASH);
            return temp4.replace("*", DASH);
        } else {
            return "untitle_" + System.currentTimeMillis() + ".png";
        }
    }

    public static final int ILLUST_TITLE = 1;
    public static final int ILLUST_ID = 2;
    public static final int P_SIZE = 3;
    public static final int USER_ID = 4;
    public static final int USER_NAME = 5;
    public static final int ILLUST_SIZE = 6;
    public static final int CREATE_TIME = 7;

    // Novel custom name codes
    public static final int NOVEL_TITLE = 101;
    public static final int NOVEL_ID = 102;
    public static final int NOVEL_USER_ID = 103;
    public static final int NOVEL_USER_NAME = 104;
    public static final int NOVEL_CREATE_DATE = 105;
    public static final int NOVEL_SERIES_TITLE = 106;
    public static final int NOVEL_SERIES_ID = 107;
    public static final int NOVEL_SERIES_RANGE = 108;

    public static String customFileName(IllustsBean illustsBean, int index) {
        List<CustomFileNameCell> result;
        String sSettingsFileNameJson = Shaft.sSettings.getFileNameJson();
        if (TextUtils.isEmpty(sSettingsFileNameJson)) {
            result = defaultFileCells();
        } else {
            result = new ArrayList<>(Shaft.sGson.fromJson(sSettingsFileNameJson,
                    new TypeToken<List<CustomFileNameCell>>() {}.getType()));
        }
        String fileUrl;
        if (illustsBean.getPage_count() == 1) {
            fileUrl = illustsBean.getMeta_single_page().getOriginal_image_url();
        } else {
            fileUrl = illustsBean.getMeta_pages().get(index).getImage_urls().getOriginal();
        }
        String ret = deleteSpecialWords(illustToFileName(illustsBean, result, index) +
                "." + getMimeTypeFromUrl(fileUrl));
        return ret;
    }

    public static String customGifFileName(IllustsBean illustsBean){
        List<CustomFileNameCell> result;
        String sSettingsFileNameJson = Shaft.sSettings.getFileNameJson();
        if (TextUtils.isEmpty(sSettingsFileNameJson)) {
            result = defaultFileCells();
        } else {
            result = new ArrayList<>(Shaft.sGson.fromJson(sSettingsFileNameJson,
                    new TypeToken<List<CustomFileNameCell>>() {}.getType()));
        }
        return Common.removeFSReservedChars(illustToFileName(illustsBean, result, 0) + ".gif");
    }

    public static String getMimeTypeFromUrl(String url) {
        String result = "png";
        if (url.contains(".")) {
            result = url.substring(url.lastIndexOf(".") + 1);
        }
        Common.showLog("getMimeType fileUrl: " + url + ", fileType: " + result);
        return result;
    }

    public static String customFileNameForPreview(IllustsBean illustsBean,
                                                  List<CustomFileNameCell> cells, int index) {
        String fileUrl;
        if (illustsBean.getPage_count() == 1) {
            fileUrl = illustsBean.getMeta_single_page().getOriginal_image_url();
        } else {
            fileUrl = illustsBean.getMeta_pages().get(index).getImage_urls().getOriginal();
        }
        return deleteSpecialWords(illustToFileName(illustsBean, cells, index) +
                "." + getMimeTypeFromUrl(fileUrl));
    }

    private static String illustToFileName(IllustsBean illustsBean,
                                           List<CustomFileNameCell> result, int index) {
        String fileName = "";
        for (int i = 0; i < result.size(); i++) {
            CustomFileNameCell cell = result.get(i);
            if (cell.isChecked()) {
                switch (cell.getCode()) {
                    case ILLUST_ID:
                        if (!TextUtils.isEmpty(fileName)) {
                            fileName = fileName + "_" + illustsBean.getId();
                        } else {
                            fileName = String.valueOf(illustsBean.getId());
                        }
                        break;
                    case ILLUST_TITLE:
                        if (!TextUtils.isEmpty(fileName)) {
                            fileName = fileName + "_" + illustsBean.getTitle();
                        } else {
                            fileName = illustsBean.getTitle();
                        }
                        break;
                    case P_SIZE:
                        if (Shaft.sSettings.isHasP0()) {
                            if (!TextUtils.isEmpty(fileName)) {
                                fileName = fileName + "_p" + index;
                            } else {
                                fileName = "p" + index;
                            }
                        } else {
                            if (illustsBean.getPage_count() != 1) {
                                if (!TextUtils.isEmpty(fileName)) {
                                    fileName = fileName + "_p" + (index + 1);
                                } else {
                                    fileName = "p" + (index + 1);
                                }
                            }
                        }
                        break;
                    case USER_ID:
                        if (!TextUtils.isEmpty(fileName)) {
                            fileName = fileName + "_" + illustsBean.getUser().getId();
                        } else {
                            fileName = String.valueOf(illustsBean.getUser().getId());
                        }
                        break;
                    case USER_NAME:
                        if (!TextUtils.isEmpty(fileName)) {
                            fileName = fileName + "_" + illustsBean.getUser().getName();
                        } else {
                            fileName = illustsBean.getUser().getName();
                        }
                        break;
                    case ILLUST_SIZE:
                        if (!TextUtils.isEmpty(fileName)) {
                            fileName = fileName + "_" + illustsBean.getWidth() + "px*" + illustsBean.getHeight() + "px";
                        } else {
                            fileName = illustsBean.getWidth() + "px*" + illustsBean.getHeight() + "px";
                        }
                        break;
                    case CREATE_TIME:
                        String createDate = Common.getLocalYYYYMMDDHHMMSSFileString(illustsBean.getCreate_date());
                        if (!TextUtils.isEmpty(fileName)) {
                            fileName = fileName + "_" + createDate;
                        } else {
                            fileName = createDate;
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        return fileName;
    }

    public static List<CustomFileNameCell> defaultFileCells() {
        List<CustomFileNameCell> cells = new ArrayList<>();
        cells.add(new CustomFileNameCell("作品标题", "作品标题，可选项", 1, true));
        cells.add(new CustomFileNameCell("作品ID", "不选的话可能两个文件名重复，导致下载失败，必选项", 2, true));
        cells.add(new CustomFileNameCell("作品P数", "显示当前图片是作品的第几P，必选项", 3, true));
        cells.add(new CustomFileNameCell("画师ID", "画师ID，可选项", 4, false));
        cells.add(new CustomFileNameCell("画师昵称", "画师昵称，可选项", 5, false));
        cells.add(new CustomFileNameCell("作品尺寸", "显示当前图片的尺寸信息，可选项", 6, false));
        cells.add(new CustomFileNameCell("创作时间", "创作时间，可选项", 7, false));
        return cells;
    }

    // -------------------- Novel custom file name --------------------

    public static List<CustomFileNameCell> defaultNovelFileCells() {
        List<CustomFileNameCell> cells = new ArrayList<>();
        // 默认不带 ID（用户嫌太长），但仍提供可选项
        cells.add(new CustomFileNameCell("小说标题", "小说标题，建议保留", NOVEL_TITLE, true));
        cells.add(new CustomFileNameCell("小说ID", "避免同名覆盖/下载失败，可选", NOVEL_ID, false));
        cells.add(new CustomFileNameCell("作者ID", "作者ID，可选", NOVEL_USER_ID, false));
        cells.add(new CustomFileNameCell("作者昵称", "作者昵称，可选", NOVEL_USER_NAME, false));
        cells.add(new CustomFileNameCell("创作日期", "仅日期(YYYY-MM-DD)，可选", NOVEL_CREATE_DATE, false));
        cells.add(new CustomFileNameCell("系列标题", "若属于系列，则加入系列标题，可选", NOVEL_SERIES_TITLE, true));
        cells.add(new CustomFileNameCell("系列ID", "系列ID，可选", NOVEL_SERIES_ID, false));
        cells.add(new CustomFileNameCell("系列章节范围", "例如 1~12，可选（系列下载时建议开启）", NOVEL_SERIES_RANGE, false));
        return cells;
    }

    public static String customNovelFileName(NovelBean novelBean) {
        List<CustomFileNameCell> cells;
        String json = Shaft.sSettings.getNovelFileNameJson();
        if (TextUtils.isEmpty(json)) {
            cells = defaultNovelFileCells();
        } else {
            cells = new ArrayList<>(Shaft.sGson.fromJson(json,
                    new TypeToken<List<CustomFileNameCell>>() {}.getType()));
        }
        String base = novelToFileName(novelBean, null, cells);
        if (TextUtils.isEmpty(base)) {
            base = "Novel_" + novelBean.getId();
        }
        return deleteSpecialWords(base + ".txt");
    }

    public static String customNovelSeriesFileName(NovelSeriesItem seriesItem) {
        // 系列下载时没有单章 novelId，这里主要用系列信息
        List<CustomFileNameCell> cells;
        String json = Shaft.sSettings.getNovelFileNameJson();
        if (TextUtils.isEmpty(json)) {
            cells = defaultNovelFileCells();
        } else {
            cells = new ArrayList<>(Shaft.sGson.fromJson(json,
                    new TypeToken<List<CustomFileNameCell>>() {}.getType()));
        }
        String base = seriesToFileName(seriesItem, cells);
        if (TextUtils.isEmpty(base)) {
            base = "Chapter_1~" + seriesItem.getContent_count() + "_" + seriesItem.getTitle();
        }
        return deleteSpecialWords(base + ".txt");
    }

    public static String customNovelFileNameForPreview(NovelBean novelBean,
                                                       List<CustomFileNameCell> cells) {
        String base = novelToFileName(novelBean, null, cells);
        if (TextUtils.isEmpty(base)) {
            base = "Novel_" + novelBean.getId();
        }
        return deleteSpecialWords(base + ".txt");
    }

    public static String customNovelSeriesFileNameForPreview(NovelSeriesItem seriesItem,
                                                             List<CustomFileNameCell> cells) {
        String base = seriesToFileName(seriesItem, cells);
        if (TextUtils.isEmpty(base)) {
            base = "Chapter_1~" + seriesItem.getContent_count() + "_" + seriesItem.getTitle();
        }
        return deleteSpecialWords(base + ".txt");
    }

    private static String novelToFileName(NovelBean novelBean, String seriesRange,
                                          List<CustomFileNameCell> cells) {
        String fileName = "";
        for (CustomFileNameCell cell : cells) {
            if (!cell.isChecked()) continue;
            switch (cell.getCode()) {
                case NOVEL_TITLE:
                    fileName = append(fileName, novelBean.getTitle());
                    break;
                case NOVEL_ID:
                    fileName = append(fileName, String.valueOf(novelBean.getId()));
                    break;
                case NOVEL_USER_ID:
                    fileName = append(fileName, String.valueOf(novelBean.getUser().getId()));
                    break;
                case NOVEL_USER_NAME:
                    fileName = append(fileName, novelBean.getUser().getName());
                    break;
                case NOVEL_CREATE_DATE:
                    // create_date 格式一般是 2020-07-07T00:30:02+09:00
                    try {
                        String d = novelBean.getCreate_date();
                        if (!TextUtils.isEmpty(d) && d.length() >= 10) {
                            fileName = append(fileName, d.substring(0, 10));
                        }
                    } catch (Exception ignore) {}
                    break;
                case NOVEL_SERIES_TITLE:
                    if (novelBean.getSeries() != null && !TextUtils.isEmpty(novelBean.getSeries().getTitle())) {
                        fileName = append(fileName, novelBean.getSeries().getTitle());
                    }
                    break;
                case NOVEL_SERIES_ID:
                    if (novelBean.getSeries() != null) {
                        fileName = append(fileName, String.valueOf(novelBean.getSeries().getId()));
                    }
                    break;
                case NOVEL_SERIES_RANGE:
                    if (!TextUtils.isEmpty(seriesRange)) {
                        fileName = append(fileName, seriesRange);
                    }
                    break;
                default:
                    break;
            }
        }
        return fileName;
    }

    private static String seriesToFileName(NovelSeriesItem seriesItem,
                                           List<CustomFileNameCell> cells) {
        String range = "Chapter_1~" + seriesItem.getContent_count();
        // 用一个“伪 NovelBean”字段映射太麻烦，这里直接按 cells 拼
        String fileName = "";
        for (CustomFileNameCell cell : cells) {
            if (!cell.isChecked()) continue;
            switch (cell.getCode()) {
                case NOVEL_TITLE:
                    // 系列下载没有单章标题，fallback 用系列标题
                    fileName = append(fileName, seriesItem.getTitle());
                    break;
                case NOVEL_ID:
                    // 系列下载没有小说ID，fallback 用 seriesId
                    fileName = append(fileName, String.valueOf(seriesItem.getId()));
                    break;
                case NOVEL_SERIES_TITLE:
                    fileName = append(fileName, seriesItem.getTitle());
                    break;
                case NOVEL_SERIES_ID:
                    fileName = append(fileName, String.valueOf(seriesItem.getId()));
                    break;
                case NOVEL_SERIES_RANGE:
                    fileName = append(fileName, range);
                    break;
                default:
                    break;
            }
        }
        return fileName;
    }

    private static String append(String base, String value) {
        if (TextUtils.isEmpty(value)) return base;
        if (TextUtils.isEmpty(base)) return value;
        return base + "_" + value;
    }
}
