import { Component, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ISessionPackage } from 'app/entities/session-package/session-package.model';
import { SessionPackageService } from 'app/entities/session-package/service/session-package.service';
import { IAthlete } from '../athlete.model';
import { AthleteService } from '../service/athlete.service';
import { AthleteFormService, AthleteFormGroup } from './athlete-form.service';

@Component({
  standalone: true,
  selector: 'jhi-athlete-update',
  templateUrl: './athlete-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class AthleteUpdateComponent implements OnInit {
  isSaving = false;
  athlete: IAthlete | null = null;

  sessionPackagesCollection: ISessionPackage[] = [];

  editForm: AthleteFormGroup = this.athleteFormService.createAthleteFormGroup();

  constructor(
    protected athleteService: AthleteService,
    protected athleteFormService: AthleteFormService,
    protected sessionPackageService: SessionPackageService,
    protected activatedRoute: ActivatedRoute,
  ) {}

  compareSessionPackage = (o1: ISessionPackage | null, o2: ISessionPackage | null): boolean =>
    this.sessionPackageService.compareSessionPackage(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ athlete }) => {
      this.athlete = athlete;
      if (athlete) {
        this.updateForm(athlete);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const athlete = this.athleteFormService.getAthlete(this.editForm);
    if (athlete.id !== null) {
      this.subscribeToSaveResponse(this.athleteService.update(athlete));
    } else {
      this.subscribeToSaveResponse(this.athleteService.create(athlete));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IAthlete>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(athlete: IAthlete): void {
    this.athlete = athlete;
    this.athleteFormService.resetForm(this.editForm, athlete);

    this.sessionPackagesCollection = this.sessionPackageService.addSessionPackageToCollectionIfMissing<ISessionPackage>(
      this.sessionPackagesCollection,
      athlete.sessionPackage,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.sessionPackageService
      .query({ filter: 'athlete-is-null' })
      .pipe(map((res: HttpResponse<ISessionPackage[]>) => res.body ?? []))
      .pipe(
        map((sessionPackages: ISessionPackage[]) =>
          this.sessionPackageService.addSessionPackageToCollectionIfMissing<ISessionPackage>(sessionPackages, this.athlete?.sessionPackage),
        ),
      )
      .subscribe((sessionPackages: ISessionPackage[]) => (this.sessionPackagesCollection = sessionPackages));
  }
}
