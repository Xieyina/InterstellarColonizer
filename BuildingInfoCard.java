package tfgirls.project.javarts.View;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import tfgirls.project.javarts.Model.Building.Building;
import tfgirls.project.javarts.Model.Building.BuildingFunction;

public class BuildingInfoCard extends VBox {
        private Building selectedBuilding;
        private final HBox root = new HBox();

        /**
         * 新建一张建筑信息卡片，放在人员底栏（{@link PeopleFooter}）里，
         * 上面显示建筑名称、工人数量和居民数量。
         *
         * @param building 这张卡片上的建筑
         * @param selectedBuilding 用户点选的建筑
         */
        public BuildingInfoCard(Building building, Building selectedBuilding) {
                root.setPadding(new Insets(10));
                root.setSpacing(15);
                root.setAlignment(Pos.CENTER);

                this.selectedBuilding = selectedBuilding;

                VBox house = new VBox();
                ImageView houseView = new ImageView(new Image(
                                getClass().getResource(ImagePath.getBuildingSpritePath(building.getType()))
                                                .toExternalForm()));
                houseView.setFitWidth(100);
                houseView.setFitHeight(100);

                // 建筑图片下方的文字标签
                Label farmLabel = new Label(building.getName());
                farmLabel.setFont(Font.font("Arial", 18));
                farmLabel.setTextFill(Color.DARKCYAN);

                // 下半部分——图标和文字
                VBox bottomSection = new VBox(50);
                bottomSection.setPrefHeight(100);
                bottomSection.setAlignment(Pos.CENTER);

                for (BuildingFunction buildingFunction : building.getFunctions()) {
                        switch (buildingFunction) {
                                case LIVING:
                                        HBox personBox = createPeopleRow(
                                                        true,
                                                        String.valueOf(building.getInhabitants().size()),
                                                        String.valueOf(building.getMaxInhabitants()));
                                        bottomSection.getChildren().add(personBox);
                                        break;
                                case WORKING:
                                        HBox workerBox = createPeopleRow(
                                                        false,
                                                        String.valueOf(building.getWorkers().size()),
                                                        String.valueOf(building.getMaxWorkers()));
                                        bottomSection.getChildren().add(workerBox);
                                        break;
                        }
                }
                house.getChildren().addAll(houseView, farmLabel);
                root.getChildren().addAll(house, bottomSection);
                // root.setPrefSize(500,250);

                this.getChildren().addAll(root);
                setBackground(building);
        }

        /**
         * 新建一行：左边小人图片（男女两个），右边文字。
         * 用法：小人图标  当前数量/最大数量
         *
         * @param residents true 表示居民（用男女居民小人），false 表示工人（用男女工人小人）
         * @param current 当前居民/工人数量
         * @param max 最大居民/工人数量
         * @return 图片在左、文字在右的 {@link HBox}
         */
        private HBox createPeopleRow(boolean residents, String current, String max) {
                // ===== 改动：用男女两张小人图表示居民/工人 =====
                String malePath = residents
                                ? "/tfgirls/project/javarts/icons/people/male_resident.png"
                                : "/tfgirls/project/javarts/icons/people/male_worker.png";
                String femalePath = residents
                                ? "/tfgirls/project/javarts/icons/people/female_resident.png"
                                : "/tfgirls/project/javarts/icons/people/female_worker.png";

                ImageView maleView = new ImageView(new Image(getClass().getResource(malePath).toExternalForm()));
                maleView.setFitWidth(24);
                maleView.setFitHeight(24);
                ImageView femaleView = new ImageView(new Image(getClass().getResource(femalePath).toExternalForm()));
                femaleView.setFitWidth(24);
                femaleView.setFitHeight(24);

                Label label = new Label(current + "/" + max);
                label.setFont(Font.font("Arial", 14));
                label.setTextFill(Color.BLACK);

                HBox hbox = new HBox(6);
                hbox.setAlignment(Pos.CENTER_LEFT);
                hbox.getChildren().addAll(maleView, femaleView, label);
                return hbox;
        }

        /**
         * 根据用户有没有点过这张卡片来设置背景。
         * @param b 这张卡片上的 {@link Building}
         */
        public void setBackground(Building b) {
                if (b == this.selectedBuilding) {
                        BackgroundImage backgroundImage = new BackgroundImage(
                                        new Image(getClass().getResource(
                                                        "/tfgirls/project/javarts/buildingCards/selected_info_background.png")
                                                        .toExternalForm()),
                                        BackgroundRepeat.NO_REPEAT, // 横向不重复
                                        BackgroundRepeat.NO_REPEAT, // 纵向不重复
                                        BackgroundPosition.CENTER, // 居中
                                        new BackgroundSize(
                                                        BackgroundSize.DEFAULT.getWidth(),
                                                        BackgroundSize.DEFAULT.getHeight(),
                                                        true, true, true, false // 自动拉伸填满容器
                                        ));

                        root.setBackground(new Background(backgroundImage));
                } else {
                        BackgroundImage backgroundImage = new BackgroundImage(
                                        new Image(getClass()
                                                        .getResource("/tfgirls/project/javarts/buildingCards/not_selected_info_background.png")
                                                        .toExternalForm()),
                                        BackgroundRepeat.NO_REPEAT,
                                        BackgroundRepeat.NO_REPEAT,
                                        BackgroundPosition.CENTER,
                                        new BackgroundSize(
                                                        BackgroundSize.DEFAULT.getWidth(),
                                                        BackgroundSize.DEFAULT.getHeight(),
                                                        true, true, true, false));

                        root.setBackground(new Background(backgroundImage));
                }
        }

}
