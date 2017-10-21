# gcloud
## Installation
### Problems with installing tensorboard on MacOS
Even with python3 (via virtualenv) you might still have an error:
```
  File ".../tf/bin/tensorboard", line 69, in FindModuleSpace
    for mod in site.getsitepackages():
AttributeError: module 'site' has no attribute 'getsitepackages'
```
It might be fixed with replacing the line with:
```python
for mod in [os.path.realpath(os.path.dirname(site.__file__) + "/site-packages")]:
```
