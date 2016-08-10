package co.ledger.manager.web.controllers.manager

import biz.enef.angulate.Module.RichModule
import biz.enef.angulate.core.Location
import biz.enef.angulate.{Controller, Scope}
import co.ledger.manager.web.Application
import co.ledger.manager.web.services.{DeviceService, WindowService}

import scala.concurrent.Future
import scala.scalajs.js
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js.JSON

/**
  *
  * AppListController
  * ledger-manager-chrome
  *
  * Created by Pierre Pollastri on 09/08/2016.
  *
  * The MIT License (MIT)
  *
  * Copyright (c) 2016 Ledger
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  */
class AppListController(val windowService: WindowService,
                        deviceService: DeviceService,
                        $scope: Scope,
                        $location: Location,
                        $route: js.Dynamic) extends Controller with ManagerController {

  var applications = js.Array[js.Dictionary[js.Any]]()
  var firmwares = js.Array[js.Dictionary[js.Any]]()

  def fetchApplications(): Future[Unit] = {
    Application.httpClient.get("/applications").json map {
      case (json, _) =>
        if (json.has("nanos")) {
          val apps = json.getJSONArray("nanos")
          applications = JSON.parse(apps.toString).asInstanceOf[js.Array[js.Dictionary[js.Any]]]
        }
    }
  }

  def fetchFirmware(): Future[Unit] = {
    Application.httpClient.get("/firmwares").json map {
      case (json, _) =>
        if (json.has("nanos")) {
          val firms = json.getJSONArray("nanos")
          js.Dynamic.global.console.log(JSON.parse(firms.toString).asInstanceOf[js.Array[js.Dictionary[js.Any]]])
          firmwares = JSON.parse(firms.toString).asInstanceOf[js.Array[js.Dictionary[js.Any]]]
        }
    }
  }

  def install(pkg: js.Dynamic): Unit = {
    js.Dynamic.global.console.log(pkg, s"/apply/install/${JSON.stringify(pkg)}/")
    $location.path(s"/apply/install/${JSON.stringify(pkg)}")
    $route.reload()
  }

  def uninstall(pkg: js.Dynamic): Unit = {
    js.Dynamic.global.console.log(pkg)
    val params = js.Dynamic.literal(
      appName = pkg,
      targetId = 0x31100002
    )
    $location.path(s"/apply/uninstall/${JSON.stringify(params)}")
    $route.reload()
  }

  fetchApplications() flatMap {(_) =>
    fetchFirmware()
  } onComplete {
    case Success(_) => $scope.$apply()
    case Failure(ex) =>
      ex.printStackTrace()
  }

}

object AppListController {

  def init(module: RichModule) = module.controllerOf[AppListController]("AppListController")

}