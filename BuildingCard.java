package tfgirls.project.javarts.View;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.Building.BuildingBuilder;
import tfgirls.project.javarts.Model.Building.BuildingFunction;
import tfgirls.project.javarts.Model.Building.BuildingType;
import tfgirls.project.javarts.Model.Position;
import tfgirls.project.javarts.Model.Resource.ResourceType;

import java.util.Map;

public class BuildingCard extends VBox {
    private BuildingType currentlySelected;
    private final VBox root = new VBox();
    private final BuildingType buildingType;


    /**
     * 新建一张建筑卡片。
     * 卡片上会显示这个建筑的各种信息，比如能住多少人、能干多少活、每天消耗和产出多少资源。
     * 鼠标放上去（悬停）可以看到建造花费。
     * 点一下就选中这张卡片（选中的建筑类型 {@link BuildingType} 会存到 currentlySelected 字段里）。
     *
     * @param buildingType 这张卡片对应的 {@link BuildingType}
     */
    public BuildingCard(BuildingType buildingType) {
        Building b = new BuildingBuilder().build(buildingType, new Position(0,0));
        this.buildingType = buildingType;

        root.setPadding(new Insets(10));
        root.setSpacing(10);
        root.setAlignment(Pos.CENTER);

        // 上半部分——图片和文字
        HBox topSection = new HBox(20);
        topSection.setAlignment(Pos.CENTER);

        ImageView houseView = new ImageView(new Image(getClass().getResource(ImagePath.getBuildingSpritePath(buildingType)).toExternalForm()));
        houseView.setFitWidth(100);
        houseView.setFitHeight(100);

        // 建筑图片下方的文字标签
        Label farmLabel = new Label(b.getName());
        farmLabel.setFont(Font.font("Arial", 18));
        farmLabel.setTextFill(Color.DARKCYAN);

        // 下半部分——左右两边的图标和文字
        HBox bottomSection = new HBox(50);
        bottomSection.setPrefHeight(100);
        bottomSection.setAlignment(Pos.CENTER);

        for (BuildingFunction buildingFunction : b.getFunctions()) {
            switch (buildingFunction) {
                case LIVING :
                    VBox personBox = createSpriteWithLabel("/tfgirls/project/javarts/icons/house.png", String.valueOf(b.getMaxInhabitants()));
                    topSection.getChildren().add(personBox);
                    break;
                case WORKING :
                    VBox workerBox = createSpriteWithLabel("/tfgirls/project/javarts/icons/worker.png", String.valueOf(b.getMaxWorkers()));
                    topSection.getChildren().add(workerBox);
                    break;
                case CONSUMING :
                    Map<ResourceType,Integer> resCons = b.getDailyConsumption();
                    VBox consumingBox = new VBox();
                    consumingBox.getChildren().add(new Label("每天消耗"));
                    for(ResourceType resourceType : resCons.keySet()) {

                        HBox resBox = createSpriteWithTextRight(ImagePath.getResourceLogoPath(resourceType), String.valueOf(resCons.get(resourceType)));

                        consumingBox.getChildren().addAll(resBox);
                    }

                    bottomSection.getChildren().add(consumingBox);
                    break;
                case PRODUCING :
                    Map<ResourceType,Integer> resProd = b.getDailyProduction();
                    VBox producingBox = new VBox();
                    producingBox.getChildren().add(new Label("每天产出"));
                    for(ResourceType resourceType : resProd.keySet()) {

                        HBox resBox = createSpriteWithTextRight(ImagePath.getResourceLogoPath(resourceType), String.valueOf(resProd.get(resourceType)));

                        producingBox.getChildren().addAll(resBox);
                    }

                    bottomSection.getChildren().add(producingBox);
                    break;
            }
        }

        root.getChildren().addAll(topSection, houseView, farmLabel, bottomSection);
        root.setPrefSize(250,400);
        setBackground(currentlySelected);

        this.getChildren().addAll(root);
        createAndLinkToolTip(b);
    }

    /**
     * @return 这张卡片对应的 {@link BuildingType}
     */
    public BuildingType getBuildingType() {
        return buildingType;
    }

    /**
     * 新建一个竖排盒子，上面放图片、下面放文字
     *
     * @param imagePath 要加载的图片路径 {@link String}
     * @param labelText 文字标签上显示的内容 {@link String}
     * @return 图片在上、文字在下的 {@link VBox}
     */
    private VBox createSpriteWithLabel(String imagePath, String labelText){
        ImageView imageView = new ImageView(new Image(getClass().getResource(imagePath).toExternalForm()));
        imageView.setFitWidth(40);
        imageView.setFitHeight(40);

        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", 14));
        label.setTextFill(Color.BLACK);

        VBox vbox = new VBox(5);
        vbox.setAlignment(Pos.CENTER);
        vbox.getChildren().addAll(imageView, label);
        return vbox;
    }

    /**
     * 和上面的方法类似，但文字放在图片右边。
     * @param imagePath 要加载的图片路径 {@link String}
     * @param labelText 文字标签上显示的内容 {@link String}
     * @return 图片在左、文字在右的 {@link HBox}
     */
    private HBox createSpriteWithTextRight(String imagePath, String labelText) {
        ImageView imageView = new ImageView(new Image(getClass().getResource(imagePath).toExternalForm()));
        imageView.setFitWidth(40);
        imageView.setFitHeight(40);

        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", 14));
        label.setTextFill(Color.BLACK);

        HBox hbox = new HBox(10);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.getChildren().addAll(imageView, label);
        return hbox;
    }

    /**
     * 根据用户有没有点过这张卡片来设置背景。
     * @param buildingType 这张卡片对应的 {@link BuildingType}
     */
    public void setBackground(BuildingType buildingType) {
        if (buildingType == this.buildingType) {
            BackgroundImage backgroundImage = new BackgroundImage(
                    new Image(getClass().getResource("/tfgirls/project/javarts/buildingCards/selected_background.png").toExternalForm()),
                    BackgroundRepeat.NO_REPEAT,    // 横向不重复
                    BackgroundRepeat.NO_REPEAT,    // 纵向不重复
                    BackgroundPosition.CENTER,     // 居中
                    new BackgroundSize(
                            BackgroundSize.DEFAULT.getWidth(),
                            BackgroundSize.DEFAULT.getHeight(),
                            true, true, true, false // 自动拉伸填满容器
                    )
            );

            root.setBackground(new Background(backgroundImage));
        } else {
            BackgroundImage backgroundImage = new BackgroundImage(
                    new Image(getClass().getResource("/tfgirls/project/javarts/buildingCards/not_selected_background.png").toExternalForm()),
                    BackgroundRepeat.NO_REPEAT,    // 横向不重复
                    BackgroundRepeat.NO_REPEAT,    // 纵向不重复
                    BackgroundPosition.CENTER,     // 居中
                    new BackgroundSize(
                            BackgroundSize.DEFAULT.getWidth(),
                            BackgroundSize.DEFAULT.getHeight(),
                            true, true, true, false // 自动拉伸填满容器
                    )
            );

            root.setBackground(new Background(backgroundImage));
        }
    }

    /**
     * 把当前选中的建筑类型设为指定的类型
     * @param buildingType
     */
    public void setSelected(BuildingType buildingType){
        currentlySelected = buildingType;
        setBackground(buildingType);
    }

    /**
     * 新建鼠标悬停时显示的提示框。
     * 里面展示这个建筑的建造花费。
     * @param building 要显示花费的 {@link Building}
     */
     public void createAndLinkToolTip(Building building) {
         VBox tooltipContent = new VBox(5);
         Label lbl = new Label("建造花费");
         lbl.setFont(Font.font("Arial", 18));
         tooltipContent.getChildren().add(lbl);
         Map<ResourceType,Integer> cost = building.getCost();
         for(ResourceType resourceType : cost.keySet()) {
             HBox resourceDisplay = new HBox();

             ImageView logo = new ImageView(new Image(getClass().getResource(ImagePath.getResourceLogoPath(resourceType)).toExternalForm()));
             logo.setFitWidth(30); // 设置图标的宽度
             logo.setFitHeight(30); // 设置图标的高度


             Label quantityLabel = new Label(String.valueOf(cost.get(resourceType)));
             quantityLabel.setFont(Font.font("Arial", 18));
             resourceDisplay.getChildren().addAll(logo, quantityLabel);
             tooltipContent.getChildren().add(resourceDisplay);
         }

         Tooltip tooltip = new Tooltip();
         tooltip.setGraphic(tooltipContent);

         Tooltip.install(root, tooltip);
     }

}
