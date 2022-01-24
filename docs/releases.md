---
layout: page
title: Änderungen
redirect_from:
  - /release-notes/
---

<ul>
{% for item in site.release-notes reversed %}
  <li><a href="{{ item.url }}">{{ item.title }}</a></li>
{% endfor %}
</ul>
