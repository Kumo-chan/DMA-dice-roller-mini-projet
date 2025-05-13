# HEIG-VD-DMA-L3

Authors :

- Patrick Furrer
- Simon Guggisberg
- Jonas Troeltsch

## Implémentation

Au niveau de l'échange de données _Current Time_ Nous avons décidé d'utiliser HOUR plutôt que
HOUR_OF_DAY après un test nous renseignant que le premier correspondait à l'heure correcte
actuelle.
Nous n'avons pas récupéré les valeurs de _Fraction_ et _Reason_ car ces valeurs sont inutiles dans
le cadre de ce laboratoire.
Finalement nous n'envoyons pas non plus ces deux valeurs, les laissant à `0`, par défaut, lors de
l'envoi à l'écran connecté BLE.

## Questions théoriques

> 5.1 La caractéristique permettant de lire la température retourne la valeur en degrés Celsius,
> multipliée par 10, sous la forme d’un entier non-signé de 16 bits.
> Quel est l’intérêt de procéder de la sorte ?

Afin de disposer d'éviter de passer par une réprésentation de type float ou double.
Cela permet d'encoder un chiffre après la virgule, et d'avoir une précision décimale.
Ainsi on pourra afficher 36.9° en cas de fièvre, 22.2° pour une température ambiante, etc.

> Pourquoi ne pas échanger un nombre à virgule flottante de type float par exemple ?

Car capter la température n'est pas d'une grande précision.
Utiliser un float pour une telle valeur est un gaspillage de communication puisque la place occupée
par celui-ci est de 4 bytes versus 2 bytes pour un Uint16LE.
Les coûts de communication seraient donc doublés pour un usage qui ne seraient pas du tout
observable, ni justifiable.

> 5.2 Le niveau de charge de la pile est à présent indiqué uniquement sur l’écran du périphérique,
> mais nous souhaiterions que celui-ci puisse informer le smartphone sur son niveau de charge
> restante.
> Veuillez spécifier la(les) caractéristique(s) qui composerai(en)t un tel service, mis à
> disposition par le périphérique et permettant de communiquer le niveau de batterie restant via
> Bluetooth Low Energy.
> Pour chaque caractéristique, vous indiquerez les opérations supportées (lecture, écriture,
> notification, indication, etc.) ainsi que les données échangées et leur format.

Le UUID de ce service est 0x180F et la caractéristique du niveau de la batterie est 0x2A19.
Mais, après test, notre écran connecté ne semble pas posséder ce service.
Nous n'avons donc pas pu implémenter cette fonctionnalité.

TODO check/continue write

Sur un appareil disposant de celui-ci, il serait possible de lire le niveau de batterie, de recevoir
une notification pour certains niveaux de batterie/en cas de batterie faible.

Le type de donnée est sous la forme Uint8 avec 0x64, par exemple, qui correspond à 100% de batterie

Source : 
- https://www.bluetooth.com/specifications/assigned-numbers/
- https://www.bluetooth.com/specifications/specs/battery-service/
