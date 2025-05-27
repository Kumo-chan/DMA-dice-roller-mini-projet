# Dice Roller

Authors :

- Patrick Furrer
- Simon Guggisberg
- Jonas Troeltsch

# Descriptif

Application de partage local de jets de dé
Développement d'une application mobile permettant gérer des lancés des dés. L'application permet de
choisir un pseudo, puis de créer ou de rejoindre un groupe local temporaire (en mettant en place un
système de communication de proximité comme Google Nearby ou BLE) et de partager l'historique des
jets avec les téléphones à proximité. L'application permet de sélectionner différents types de dés (
D4, D6, D8, D10, D12, etc.) et de choisir le nombre de dés à lancer (1-10).

Fonctionnalités nice-to-have :

- animation 3D des dés étant jetés
- notification en cas de réception d'un jet de dé

Fonctionnalité avancée : Google Nearby ou Bluetooth Low Energy, évtl. 3D

# Implémentation

## Compose

une fenêtre de popup demande aux utilisateurs d'entrer un username avant d'accéder à l'application.

Un fichier `Layout` contient les différentes pages, ici sous forme de tabs, de l'application :

- Page initiale permetttant de choisir le nombre de dés à lancer et leur type. Cette page affiche
  également le dernier jet effectué par soi ou par d'autres.
- Page de paramètres listant l'historiques des derniers jets effectués par soi ou par d'autres. Un
  bouton permet également de changer le username.
- Page de rendu 3D affichant un icosaèdre afin de représenter un dé à 20 faces, emblème des TTRPG.
  Cette page ne propose pas d'interactions contrairement à la fonctionnalité nice-to-have annoncée,
  en raison de complexités d'implémentation.

## Nearby

L'utilisation d'une librairie comme Google Nearby facilite grandement l'implémentation pour une
application end-to-end device comme la notre.

## OpenGL

C'est en suivant la documentation d'OpenGL qu'une première implémentation, avec un triangle, a été
effectuée.
Le code de la `SurfaceView`, du `GLRenderer` et du `Triangle` proviennent de la documentation et ont
été adaptés au besoin de l'application.

Le fichier `Icosahedron` contient le code pour modéliser la forme d'un icosaèdre.
Ceci pourrait être répliqué pour différents types de dés, mais en raison de la complexité de
modélisation et la prise en main d'OpenGL, nous avons déclaré cela hors scope pour ce projet.

# Conclusion

La prise en main de Jetpack Compose et de Google Nearby a été relativement facile, tout le contraire
d'OpenGL qui a présenté de nombreuses difficultés et qui a demandé de changer le scope de son
utilisation de nombreuses fois.

En l'état l'application est fonctionnelle mais pourrait être améliorée pour atteindre la vision
originelle, avec des dés animés, possédant des interactions physiques, et des textures montrant le
numéro sur chaque face.
