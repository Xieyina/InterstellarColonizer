package tfgirls.project.javarts.View;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.scene.transform.Scale;

public class BuildingFooter extends ScrollPane {

    private HBox container;

    /**
     * 新建底部栏，里面放的是建筑卡片（{@link BuildingCards}）。
     * 用来先选一种建筑类型（{@link BuildingType}），再到地图上点一下放新建筑。
     */
    public BuildingFooter() {
        // 新建一个容器来装卡片
        container = new HBox(10); // 卡片之间间隔 10 像素
        container.setPadding(new Insets(10));
        container.setAlignment(Pos.CENTER_LEFT);
        container.setBackground(new Background(new BackgroundImage(
                new Image(getClass().getResource("/tfgirls/project/javarts/panel_blue.png").toExternalForm()),
                BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT, BackgroundSize.DEFAULT)));

        this.setContent(container);
        this.setFitToHeight(true); // 让滚动面板高度跟内容一样高
        this.setHbarPolicy(ScrollBarPolicy.AS_NEEDED); // 内容超宽时可以左右滚动
        this.setVbarPolicy(ScrollBarPolicy.NEVER); // 不允许上下滚动

        // 宽度跟随窗口大小变化
        this.setPrefWidth(Double.MAX_VALUE);
    }

    /**
     * 往底部栏里加一张建筑卡片。
     * 会保持卡片原来的大小比例。
     *
     * @param widget
     */
    public void addWidget(javafx.scene.Node widget) {
        Scale scale = new Scale(0.8, 0.8); // 缩小到 80%
        widget.getTransforms().add(scale);
        container.getChildren().add(widget);
    }

}
